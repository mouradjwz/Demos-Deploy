import axios from "axios";

export interface Candidate {
  id: string
  fullName: string
  firstName: string
  lastName: string
  initials: string | null
  gender: string | null
  locality: string | null
}

export interface GetCandidatesResponse {
  candidates: Candidate[]
}

export async function getCandidatesByParty(electionId: string, partyId: string): Promise<Candidate[]> {
  const response = await axios.get<GetCandidatesResponse>(
    `http://localhost:8081/api/${electionId}/candidate/party/${partyId}`
  )
  return response.data.candidates
}
