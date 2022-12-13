using my.bp as my from '../db/bp';

service cloud.sdk.capng @(requires: 'any') {
     entity CapBusinessPartner as projection on my.CapBusinessPartner;
}