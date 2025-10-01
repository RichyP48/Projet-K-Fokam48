import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

export interface SchoolDropdown {
  id: number;
  name: string;
}

export interface FacultyDropdown {
  id: number;
  name: string;
}

export interface DepartmentDropdown {
  id: number;
  name: string;
}

@Injectable({
  providedIn: 'root'
})
export class AcademicService {
  private apiUrl = `${environment.apiUrl}/academic`;

  constructor(private http: HttpClient) {}

  getSchoolDropdowns(): Observable<SchoolDropdown[]> {
    return this.http.get<SchoolDropdown[]>(`${this.apiUrl}/dropdowns/schools`);
  }

  getFacultyDropdownsBySchool(schoolId: number): Observable<FacultyDropdown[]> {
    return this.http.get<FacultyDropdown[]>(`${this.apiUrl}/dropdowns/faculties/${schoolId}`);
  }

  getDepartmentDropdownsByFaculty(facultyId: number): Observable<DepartmentDropdown[]> {
    return this.http.get<DepartmentDropdown[]>(`${this.apiUrl}/dropdowns/departments/${facultyId}`);
  }

  getAllFacultyNames(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/faculty-names`);
  }

  getAllDepartmentNames(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/department-names`);
  }
}