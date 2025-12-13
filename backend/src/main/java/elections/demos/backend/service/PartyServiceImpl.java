package elections.demos.backend.service;

import elections.demos.backend.dto.GetPartiesDTO;
import elections.demos.backend.dto.PartyDTO;
import elections.demos.backend.model.Election;
import elections.demos.backend.repository.PartyRepository;
import elections.demos.backend.utils.Constants;
import elections.demos.backend.utils.PathUtils;
import elections.demos.backend.utils.xml.DutchElectionParser;
import elections.demos.backend.utils.xml.transformers.DutchCandidateTransformer;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;

    public PartyServiceImpl(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Override
    public GetPartiesDTO getAllParties(String electionId) {
        System.out.println("Fetching parties for election: " + electionId);

        List<PartyDTO> partyDTOs = partyRepository.findByElectionElectionId(electionId)
                .stream()
                .map(party -> new PartyDTO(party.getPartyCode(), party.getName()))
                .collect(Collectors.toList());

        return new GetPartiesDTO(partyDTOs);
    }

}
