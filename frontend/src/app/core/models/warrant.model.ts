export interface WarrantCheckResult {
    documentNumber: string;
    interpolWarrant: boolean;
    domesticWarrant: boolean;
    documentReportedStolen: boolean;
    interpolReason: string | null;
    domesticReason: string | null;
    stolenReason: 'STOLEN' | 'LOST' | null;
}