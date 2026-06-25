export type GoodsCertificate =
    | 'ADR_CERTIFICATE'
    | 'VETERINARY_CERTIFICATE'
    | 'ATP_CERTIFICATE'
    | 'WASTE_TRANSPORT_CERTIFICATE'
    | 'PHARMACEUTICAL_CERTIFICATE'
    | 'RADIOACTIVE_CERTIFICATE'
    | 'EXPLOSIVES_CERTIFICATE';

export interface Driver {
    name: string;
    surname: string;
    citizenship: string;
    foreignCitizen: boolean;
    interpolWarrant: boolean;
    domesticWarrant: boolean;
    photoMatches: boolean;
    documentReportedStolen: boolean;
    hasVisa: boolean;
    hasSupplementaryDocument: boolean;
    financialFunds: number | null;
    plannedStayDays: number | null;
    dob: string | null;
}

export interface DrivingLicence {
    licenceNumber: string;
    expiryDate: string;
    category: string;
}

export interface IdentificationDocument {
    documentNumber: string;
    type: 'PASSPORT' | 'ID_CARD';
    issuingCountry: string;
    expiryDate: string;
}

export interface DriverCertificate {
    goodsCertificate: GoodsCertificate;
    yearsExperience: number;
    expiryDate: string;
}