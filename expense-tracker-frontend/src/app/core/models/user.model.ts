import {Role} from '@core/models/role.enum';

export interface User {
  id: number;
  username: string;
  email: string;
  role: Role;
  enabled: boolean;
}
