export interface ForecastDetail {
    nextMatchDate: string;
    nextMatchHomeTeam: string;
    team: string;
    opponent: string;
    teamScore?: number;
    opponentRankScore?: number;
    totalPoints: number;
}
