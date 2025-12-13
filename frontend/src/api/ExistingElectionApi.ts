import axios from "axios";

export interface Election {
  electionId: string;
  electionName: string;
}

export async function getAvailableElections(): Promise<Election[]> {
  const response = await axios.get<Election[]>("http://localhost:8081/api/elections/available");
  return response.data;
}
