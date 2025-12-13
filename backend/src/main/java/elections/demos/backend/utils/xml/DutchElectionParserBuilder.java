package elections.demos.backend.utils.xml;

/**
 * Builder for creating DutchElectionParser instances.
 * This builder allows step-by-step construction of parser instances
 * with selective inclusion of transformer components.
 */
public class DutchElectionParserBuilder {
    private DefinitionTransformer definitionTransformer;
    private CandidateTransformer candidateTransformer;
    private VotesTransformer resultTransformer;
    private VotesTransformer nationalVotesTransformer;
    private VotesTransformer constituencyVotesTransformer;
    private VotesTransformer municipalityVotesTransformer;
    private VotesTransformer partyVotesTransformer;
    private VotesTransformer seatsAndShareTransformer;

    /**
     * Sets the transformer for election definitions.
     *
     * @param definitionTransformer The transformer to process election definition files
     * @return This builder instance for method chaining
     */
    public DutchElectionParserBuilder withDefinitionTransformer(DefinitionTransformer definitionTransformer) {
        this.definitionTransformer = definitionTransformer;
        return this;
    }

    /**
     * Sets the transformer for candidates.
     *
     * @param candidateTransformer The transformer to process candidate list files
     * @return This builder instance for method chaining
     */
    public DutchElectionParserBuilder withCandidateTransformer(CandidateTransformer candidateTransformer) {
        this.candidateTransformer = candidateTransformer;
        return this;
    }

    /**
     * Sets the transformer for election results.
     *
     * @param resultTransformer The transformer to process result files
     * @return This builder instance for method chaining
     */
    public DutchElectionParserBuilder withResultTransformer(VotesTransformer resultTransformer) {
        this.resultTransformer = resultTransformer;
        return this;
    }

    /**
     * Sets the transformer for national votes.
     *
     * @param nationalVotesTransformer The transformer to process national votes files
     * @return This builder instance for method chaining
     */
    public DutchElectionParserBuilder withNationalVotesTransformer(VotesTransformer nationalVotesTransformer) {
        this.nationalVotesTransformer = nationalVotesTransformer;
        return this;
    }

    /**
     * Sets the transformer for constituency votes.
     *
     * @param constituencyVotesTransformer The transformer to process constituency votes files
     * @return This builder instance for method chaining
     */
    public DutchElectionParserBuilder withConstituencyVotesTransformer(VotesTransformer constituencyVotesTransformer) {
        this.constituencyVotesTransformer = constituencyVotesTransformer;
        return this;
    }

    /**
     * Sets the transformer for municipality votes.
     *
     * @param municipalityVotesTransformer The transformer to process municipality votes files
     * @return This builder instance for method chaining
     */
    public DutchElectionParserBuilder withMunicipalityVotesTransformer(VotesTransformer municipalityVotesTransformer) {
        this.municipalityVotesTransformer = municipalityVotesTransformer;
        return this;
    }

    public DutchElectionParserBuilder withPartyVotesTransformer(VotesTransformer partyVotesTransformer) {
        this.partyVotesTransformer = partyVotesTransformer;
        return this;
    }

    public DutchElectionParserBuilder withSeatsAndShareTransformer(VotesTransformer seatsAndShareTransformer) {
        System.out.println("Registering SeatsAndShareTransformer");
        this.seatsAndShareTransformer = seatsAndShareTransformer;
        return this;
    }


    /**
     * Builds and returns a new DutchElectionParser instance with the configured transformers.
     *
     * @return A new DutchElectionParser instance
     */
    public DutchElectionParser build() {
        return new DutchElectionParser(definitionTransformer, candidateTransformer, resultTransformer, nationalVotesTransformer, constituencyVotesTransformer, municipalityVotesTransformer, partyVotesTransformer);
    }

    /**
     * Creates a new instance of the builder.
     *
     * @return A new DutchElectionParserBuilder instance
     */
    public static DutchElectionParserBuilder create() {
        return new DutchElectionParserBuilder();
    }
}