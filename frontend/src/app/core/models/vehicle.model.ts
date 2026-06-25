import {GoodsCertificate} from "./driver.model";

export type PermitType =
    | 'CEMT'
    | 'BILATERAL'
    | 'TRANSIT'
    | 'THIRD_COUNTRY';

export interface Vehicle {
    plateNumber: string;
    totalMass: number | null;
}

export interface VehicleRegistration {
    registrationNumber: string;
    expiryDate: string;
    trailerCapacity: number | null;
    truckWeight: number | null;
    maxAllowedWeight: number | null;
}

export interface CMRDocument {
    originCountry: string;
    destinationCountry: string;
    goodsWeight: number | null;
    goodsDescription: string;
    senderIdentity: string;
    receiverIdentity: string;
}

export interface TransportPermit {
    type: PermitType;
    goodsCertificate: GoodsCertificate;
    expiryDate: string;
    coveredRoutes: string;
}

export interface LiveWeightMeasurement {
    measuredTotalWeight: number | null;
    visualSag: boolean;
}

