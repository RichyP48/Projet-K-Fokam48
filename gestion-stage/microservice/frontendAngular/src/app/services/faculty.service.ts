import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface FacultyStudent {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  filiere: string;
  telephone: string;
  active: boolean;
}

export interface FacultyCompany {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  telephone: string;
  active: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class FacultyService {
  private apiUrl = `${environment.apiUrl}/users/faculty`;

  constructor(private http: HttpClient) {}

  getMyStudents(): Observable<FacultyStudent[]> {
    return this.http.get<FacultyStudent[]>(`${this.apiUrl}/students`);
  }

  getCompaniesForMyStudents(): Observable<FacultyCompany[]> {
    return this.http.get<FacultyCompany[]>(`${this.apiUrl}/companies`);
  }

  testAccess(): Observable<any> {
    return this.http.get(`${this.apiUrl}/test`);
  }
}
