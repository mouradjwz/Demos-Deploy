// Party name abbreviations and mappings
const partyAbbreviations: Record<string, string> = {
  // 2023 Parties
  "GROENLINKS / Partij van de Arbeid (PvdA)": "GL/PvdA",
  "PVV (Partij voor de Vrijheid)": "PVV",
  "VVD": "VVD",
  "Nieuw Sociaal Contract": "NSC",
  "D66": "D66",
  "BBB": "BBB",
  "CDA": "CDA",
  "SP (Socialistische Partij)": "SP",
  "ChristenUnie": "CU",
  "Forum voor Democratie": "FvD",
  "Volt": "Volt",
  "JA21": "JA21",
  "Staatkundig Gereformeerde Partij (SGP)": "SGP",
  "DENK": "DENK",
  "50PLUS": "50+",
  "Partij voor de Dieren": "PvdD",
  "BIJ1": "BIJ1",
  "BVNL / Groep Van Haga": "BVNL",
  "Splinter": "Splinter",
  "Piratenpartij - De Groenen": "Piratenpartij",

  // 2021 Parties
  "Partij van de Arbeid (P.v.d.A.)": "PvdA",
  "GROENLINKS": "GL",
  "Democraten 66 (D66)": "D66",
  "CODE ORANJE": "CODE ORANJE",
  "NIDA": "NIDA",
  "Piratenpartij": "Piratenpartij",
  "LP (Libertaire Partij)": "LP",
  "JONG": "JONG",
  "NLBeter": "NLBeter",
  "Lijst Henk Krol": "Lijst Krol",
  "OPRECHT": "OPRECHT",
  "JEZUS LEEFT": "JEZUS LEEFT",
  "Trots op Nederland (TROTS)": "TROTS",
  "U-Buntu Connected Front": "Ubuntu",
  "Blanco (Zeven, A.J.L.B.)": "Blanco",
  "Partij van de Eenheid": "Eenheid",
  "DE FEESTPARTIJ (DFP)": "Feestpartij",
  "Vrij en Sociaal Nederland": "VSN",
  "Wij zijn Nederland": "WZN",
  "Modern Nederland": "Modern",
  "De Groenen": "Groenen",
  "Partij voor de Republiek": "Republiek",

  // 2017 Parties
  "OndernemersPartij": "Ondernemers",
  "VNL (VoorNederland)": "VNL",
  "NIEUWE WEGEN": "NIEUWE WEGEN",
  "De Burger Beweging": "Burger Beweging",
  "Vrijzinnige Partij": "Vrijzinnig",
  "GeenPeil": "GeenPeil",
  "Artikel 1": "Artikel 1",
  "Niet Stemmers": "Niet Stemmers",
  "Libertarische Partij (LP)": "LP",
  "Lokaal in de Kamer": "Lokaal",
  "StemNL": "StemNL",
  "MenS en Spirit / Basisinkomen Partij / V-R": "MenS/Spirit",
  "Vrije Democratische Partij (VDP)": "VDP"
};

// Alternative names and variations
const partyAliases: Record<string, string> = {
  "GROENLINKS / Partij van de Arbeid (PvdA)": "GL/PvdA",
  "GROENLINKS": "GL",
  "Partij van de Arbeid (P.v.d.A.)": "PvdA",
  "Partij van de Arbeid (PvdA)": "PvdA",
  "Democraten 66 (D66)": "D66",
  "SP (Socialistische Partij)": "SP",
  "Staatkundig Gereformeerde Partij (SGP)": "SGP",
  "PVV (Partij voor de Vrijheid)": "PVV",
  "Partij voor de Dieren": "PvdD",
  "50PLUS": "50+",
  "Forum voor Democratie": "FvD",
  "ChristenUnie": "CU",
  "BVNL / Groep Van Haga": "BVNL",
  "Piratenpartij - De Groenen": "Piratenpartij"
};

export function shortenPartyName(name: string): string {
  if (!name) return "";

  // Check exact matches first
  if (partyAbbreviations[name]) {
    return partyAbbreviations[name];
  }

  // Check aliases
  if (partyAliases[name]) {
    return partyAliases[name];
  }

  // Fallback: remove text in parentheses for common patterns
  const simplified = name.replace(/\s*\([^)]*\)/g, '').trim();
  if (simplified && simplified !== name) {
    return simplified;
  }

  return name;
}

export function getFullPartyName(shortName: string): string {
  // Reverse lookup - find the first matching full name
  for (const [fullName, abbreviation] of Object.entries(partyAbbreviations)) {
    if (abbreviation === shortName) {
      return fullName;
    }
  }

  for (const [fullName, abbreviation] of Object.entries(partyAliases)) {
    if (abbreviation === shortName) {
      return fullName;
    }
  }

  return shortName;
}

export const partyNames = {
  abbreviations: partyAbbreviations,
  aliases: partyAliases,
  shorten: shortenPartyName,
  getFull: getFullPartyName
};

export default partyNames;
