import { Service } from './service';

export const SERVICES: Service[] = [
  { name: 'Manage users', status: 'WORKING', url: "users" },
  { name: 'queryAPI', status: 'NOT IMPLEMENTED' },
  { name: 'Models', status: 'WORKING', url: "models" },
  { name: 'queryAPI', status: 'NOT IMPLEMENTED'},
  { name: 'evolveAPI', status: 'NOT IMPLEMENTED' },
  { name: 'backupAPI', status: 'WORKING' },
  { name: 'restoreAPI', status: 'WORKING' }
];
