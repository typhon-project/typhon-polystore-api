import { Service } from './service';

export const SERVICES: Service[] = [
  { name: 'Manage users', status: 'WORKING', url: "users" },
  { name: 'Databases', status: 'WORKING', url: "databases" },
  { name: 'Models', status: 'WORKING', url: "models", dontShowTitle: true },
  { name: 'queryAPI', status: 'NOT IMPLEMENTED'},
  { name: 'evolveAPI', status: 'NOT IMPLEMENTED' }
];
