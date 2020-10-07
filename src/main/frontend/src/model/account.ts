import { EnumRole } from './role';
import { EnumAuthProvider } from './configuration';

/**
 * Address
 */
export interface Address {
  streetName: string;
  streetNumber: string;
  city: string;
  region: string;
  country: string;
  postalCode: string;
  floorApartment: string;
}

/**
 * Activation token command
 */
export interface ActivationTokenCommandDto {
  /**
   * Email address to verify
   */
  email: string;
}

/**
 * Profile base class
 */
interface ProfileBase {
  /**
   * Provider description
   */
  additionalInfo: string;
  /**
   * Provider bank account currency
   */
  bankAccountCurrency: string;
  /**
   * Provider bank account holder name
   */
  bankAccountHolderName: string;
  /**
   * Provider bank account IBAN
   */
  bankAccountIban: string;
  /**
   *  Provider bank account SWIFT code
   */
  bankAccountSwift: string;
  /**
   * Company name
   */
  company: string;
  /**
   * Company type
   */
  companyType: string;
  /**
   * Country
   */
  country: string;
  /**
   * Country phone code
   */
  countryPhoneCode: string;
  /**
   * Public provider email address. When browsing catalogue items, only verified email addresses are returned
   */
  email: string;
  /**
   * First name
   */
  firstName: string;
  /**
   * Base64 encoded user image
   */
  image?: string;
  /**
   * User image mime type (used with image property to create a data URL)
   */
  imageMimeType?: string;
  /**
   * Last name
   */
  lastName: string;
  /**
   * Base64 encoded company logo image
   */
  logoImage?: string;
  /**
   * Company logo image mime type (used with image property to create a data URL)
   */
  logoImageMimeType?: string;
  /**
   * Provider mobile phone
   */
  mobile: string;
  /**
   * Provider contact phone
   */
  phone: string;
  /**
   * Provider official site URL
   */
  siteUrl: string;
  /**
   * Provider VAT number
   */
  vat: string;
}

/**
 * Account profile
 */
export interface Profile extends ProfileBase {
  /**
   * User list of addresses
   */
  addresses: Address[];
  /**
   * True if public email is verified
   */
  emailVerified: boolean;
  /**
   * When the public email has been verified
   */
  emailVerifiedAt: string;
  /**
   * Date of provider (publisher) registration
   */
  providerVerifiedAt: string;
  /**
   * Provider rating. If there are no ratings, null is returned
   */
  rating: number;
  /**
   * True if user has accepted the service terms of use
   */
  termsAccepted: boolean;
  /**
   * When user has accepted the service terms of use
   */
  termsAcceptedAt: string;
  /**
   * Profile creation date
   */
  createdOn: string;
  /**
   * Profile most recent update date
   */
  modifiedOn: string;
}

/**
 * Profile update command
 */
export interface ProfileCommand extends ProfileBase {
  /**
   * User list of addresses
   */
  addresses: Address[];
}

/**
 * User account
 */
export interface AccountBase {
  /**
   * User email. Must be unique
   */
  email: string;
  /**
   * Locale. Default value is set to 'en'
   */
  locale: string;
}

export interface AccountCommand extends AccountBase {
  /**
   * Account password
   */
  password: string;
  /**
   * Account password verification. Must match property password
   */
  verifyPassword: string;
  /**
   * Account profile
   */
  profile: ProfileCommand;
}

export interface Account extends AccountBase {
  /**
   * Date of account activation. Activation occurs when the user verifies his email address.
   * The date is in ISO format.
   */
  activatedAt: string;
  /**
   * Activation status
   */
  activationStatus: string;
  /**
   * True if the email address is verified
   */
  emailVerified: boolean;
  /**
   * Date of email verification
   */
  emailVerifiedAt: string;
  /**
   * IDP name used for account registration. A value from enum  {@link EnumAuthProvider}
   */
  idpName: EnumAuthProvider;
  /**
   * User name as retrieved by the IDP user info endpoint
   */
  idpUserAlias: string;
  /**
   * User image URL as retrieved by the IDP user info endpoint
   */
  idpUserImage: string;
  /**
   * Date of registration in ISO format
   */
  registeredAt: string;
  /**
   * User profile
   */
  profile: Profile;
  /**
   * User name (always equal to user email)
   */
  username: string;
  /**
   * User roles. Every authenticated user has at least role ROLE_USER
   */
  roles: EnumRole[];
}

