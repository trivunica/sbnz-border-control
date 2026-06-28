export interface CrossingEvent {
    plateNumber: string;
    driverDocumentNumber: string;
    borderCrossingId: string;
    companyName: string;
    destinationCountry: string;
    crossedAt: string;
    timestamp: number;
}

export interface CepAlarm {
    type: string;
    plateNumber: string;
    message: string;
    crossedAt?: string;
}

export interface MrzResult {
    docType: 'PASSPORT' | 'ID_CARD';
    surname: string;
    givenNames: string;
    documentNumber: string;
    nationality: string;
    issuingCountry: string;
    dateOfBirth: string;
    expiryDate: string;
    sex: string;
    checkDigitsValid: boolean;
    rawLines: string[];
}
