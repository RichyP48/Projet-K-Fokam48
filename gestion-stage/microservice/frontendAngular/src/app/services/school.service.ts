import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface School {
  id: number;
  name: string;
  description?: string;
  address?: string;
  website?: string;
}

export interface Faculty {
  id: number;
  name: string;
  description?: string;
  school: School;
}

export interface Department {
  id: number;
  name: string;
  faculty: Faculty;
  headTeacher?: any;
}

@Injectable({
  providedIn: 'root'
})
export class SchoolService {

  constructor(private apiService: ApiService) {}

  getAllSchools(): Observable<School[]> {
    return this.apiService.get<School[]>('/academic/schools');
  }

  getFacultiesBySchool(schoolId: number): Observable<Faculty[]> {
    return this.apiService.get<Faculty[]>(`/academic/faculties/${schoolId}`);
  }

  getDepartmentsByFaculty(facultyId: number): Observable<Department[]> {
    return this.apiService.get<Department[]>(`/academic/departments/${facultyId}`);
  }

  getAllSchoolNames(): Observable<string[]> {
    return this.apiService.get<string[]>('/academic/school-names');
  }

  getAllFacultyNames(): Observable<string[]> {
    return this.apiService.get<string[]>('/academic/faculty-names');
  }

  getAllDepartmentNames(): Observable<string[]> {
    return this.apiService.get<string[]>('/academic/department-names');
  }

  testConnection(): Observable<string> {
    return this.apiService.get<string>('/academic/test');
  }
}
