import axios from "axios";

export interface Party {
  id: string;
  name: string;
}

export async function getAllParties(electionId: string): Promise<Party[]> {
  const response = await axios.get(`http://localhost:8081/api/${electionId}/party/all`, {
    params: {
      folderName: `${electionId}_HvA_UvA`,
    },
  });
  return response.data.parties;
}
