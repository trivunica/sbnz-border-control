import {Driver, DriverCertificate, DrivingLicence, IdentificationDocument} from "./driver.model";
import {CMRDocument, LiveWeightMeasurement, TransportPermit, Vehicle, VehicleRegistration} from "./vehicle.model";

export type ValidityStatus =
    | 'VALID'
    | 'PROBLEM'
    | 'FORBIDDEN'
    | 'ARREST';

export type ViolationType =
    | 'EXPIRED_DRIVING_LICENCE'
    | 'INVALID_DRIVING_CATEGORY'
    | 'EXPIRED_ID_CARD'
    | 'EXPIRED_PASSPORT'
    | 'PASSPORT_SHORT_VALIDITY'
    | 'SUSPECTED_FORGERY'
    | 'STOLEN_LOST_DOCUMENT'
    | 'MISSING_VISA_SUPPLEMENT'
    | 'INSUFFICIENT_FUNDS'
    | 'TRAILER_OVERLOAD'
    | 'TOTAL_WEIGHT_OVERLOAD'
    | 'MISSING_THIRD_COUNTRY_PERMIT'
    | 'INTERPOL_WARRANT'
    | 'DOMESTIC_WARRANT'
    | 'EXPIRED_REGISTRATION'
    | 'PENDING_WEIGHT_CHECK'
    | 'MISSING_ADR_CERTIFICATE'
    | 'MISSING_VETERINARY_CERTIFICATE'
    | 'MISSING_ATP_CERTIFICATE'
    | 'MISSING_WASTE_TRANSPORT_CERTIFICATE'
    | 'MISSING_PHARMACEUTICAL_CERTIFICATE'
    | 'MISSING_RADIOACTIVE_CERTIFICATE'
    | 'MISSING_EXPLOSIVES_CERTIFICATE'
    | 'BILATERAL_THIRD_COUNTRY_VIOLATION'
    | 'BILATERAL_WEIGHT_EXCEEDED'
    | 'INSUFFICIENT_DRIVING_EXPERIENCE';

export type ActionRecommendation =
    | 'ALLOW'
    | 'HOLD_FINE'
    | 'FORBID_ENTRY'
    | 'ARREST';

export interface DriverValidity { status: ValidityStatus; }

export interface VehicleValidity { status: ValidityStatus; }

export interface PermitStatus { status: ValidityStatus; }

export interface Violation {
    type: ViolationType;
    legalBasis: string;
    fineAmount: number;
    canContinue: boolean;
    explanation: string;
}

export interface FinalDecision {
    totalFine: number;
    recommendation: ActionRecommendation;
    requiresDriverReplacement: boolean;
    requiresCargoOffLoad: boolean;
    entryRefusalCertificate: string | null;
}

export interface BorderCrossingRequest {
    driver: Driver;
    drivingLicence: DrivingLicence;
    identificationDocument: IdentificationDocument;
    vehicleRegistration?: VehicleRegistration | null;
    vehicle?: Vehicle | null;
    cmrDocument?: CMRDocument | null;
    transportPermits?: TransportPermit[];
    liveWeightMeasurement?: LiveWeightMeasurement | null;
    driverCertificates?: DriverCertificate[];
}

export interface BorderCrossingResult {
    driverValidity: DriverValidity | null;
    vehicleValidity: VehicleValidity | null;
    permitStatus: PermitStatus | null;
    violations: Violation[];
    finalDecision: FinalDecision | null;
}


