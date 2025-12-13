package elections.demos.backend.service;

import elections.demos.backend.dto.ExistingElectionDTO;
import elections.demos.backend.model.Election;
import elections.demos.backend.repository.ElectionRepository;
import elections.demos.backend.repository.PartyRepository;
import elections.demos.backend.utils.PathUtils;
import elections.demos.backend.utils.xml.DutchElectionParser;
import elections.demos.backend.utils.xml.DutchElectionParserBuilder;
import elections.demos.backend.utils.xml.transformers.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A demo service for demonstrating how an EML-XML parser can be used inside a backend application.<br/>
 * <br/>
 * <i><b>NOTE: </b>There are some TODO's and FIXME's present that need fixing!</i>
 */
@Service
public class DutchElectionServiceImpl implements DutchElectionService {
    private final ElectionRepository electionRepository;
    private final DutchPartyService dutchPartyService;
    private final PartyRepository partyRepository;

    public DutchElectionServiceImpl(ElectionRepository electionRepository, PartyRepository partyRepository, DutchPartyService dutchPartyService) {
        this.electionRepository = electionRepository;
        this.partyRepository = partyRepository;
        this.dutchPartyService = dutchPartyService;
    }

    @Transactional
    @Override
    public Election readResults(String electionId, String folderName) {
        System.out.println("Processing election files for: " + electionId);

        // Check if election already exists in the database
        Optional<Election> existingElection = electionRepository.findByElectionId(electionId);
        if (existingElection.isPresent()) {
            System.out.println("Election already exists in database, returning stored data");
            return existingElection.get();
        }

        // Create new election object for parsing
        Election election = new Election(electionId);

        // Build parser with transformers
        DutchElectionParser electionParser = DutchElectionParserBuilder.create()
                .withDefinitionTransformer(new DutchDefinitionTransformer(election))
                .withCandidateTransformer(new DutchCandidateTransformer(election))
                .withResultTransformer(new DutchResultTransformer(election))
                .withNationalVotesTransformer(new DutchNationalVotesTransformer(election))
                .withConstituencyVotesTransformer(new DutchConstituencyVotesTransformer(election))
                .withMunicipalityVotesTransformer(new DutchMunicipalityVotesTransformer(election))
                .withPartyVotesTransformer(new DutchPartyTransformer(election))
                .build();

        try {
            // Parse XML files
            electionParser.parseResults(electionId, PathUtils.getResourcePath("/%s".formatted(folderName)));

            // Save kieskring data before persisting (transient fields are lost after save)
            Map<Integer, Map<String, Long>> kieskringData = election.getKieskringPartyVotes();

            // Save to database
            election = electionRepository.save(election);
            
            // Process kieskring results using the data captured before persistence
            if (!kieskringData.isEmpty()) {
                dutchPartyService.calculateKieskringResults(electionId, kieskringData);
            }

            return election;
        } catch (IOException | XMLStreamException | NullPointerException | ParserConfigurationException |
                 SAXException e) {
            System.err.println("Failed to process the election results!");
            e.printStackTrace();
            throw new RuntimeException("Failed to process election results", e);
        }
    }


    /**
     * Gets an election by ID from the database
     *
     * @param electionId the ID of the election
     * @return the Election if found, otherwise null
     */
    public Election getElectionById(String electionId) {
        return electionRepository.findByElectionId(electionId).orElse(null);
    }

    /**
     * Checks if an election exists in the database
     *
     * @param electionId the ID of the election
     * @return true if the election exists, false otherwise
     */
    public boolean electionExists(String electionId) {
        return electionRepository.existsByElectionId(electionId);
    }

    /**
     * Retrieves a list of available election IDs from the database.
     *
     * @return a list of election IDs
     */
    public List<ExistingElectionDTO> getAvailableElections() {
        return electionRepository.findAll()
                .stream()
                .map(election -> new ExistingElectionDTO(election.getElectionId(), election.getElectionName()))
                .toList();
    }
}