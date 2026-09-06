export interface MatchResult {
    resultId: number;
    leagueName: string;
    homeTeam: string;
    awayTeam: string;
    homeTeamScore?: number | null;
    awayTeamScore?: number | null;
    matchDate: string;
    matchStatus: string;
    homeTeamHasWon: boolean;
    homeTeamHasLost: boolean;
    round: number;
    isCurrent: boolean;
}
