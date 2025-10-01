import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { ApiService } from './api.service';
import { AuthResponse, CompanyRegistrationRequest, LoginRequest, StudentRegistrationRequest, User, UserRole } from '../models/user.model';


@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  private tokenKey = 'auth_token';
  private userIdKey = 'user_id';
  private userRoleKey = 'user_role';
  
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private apiService: ApiService) {
    console.log('🔐 AuthService initialized');
    // Check if user is already logged in on application start
    this.loadUserFromStorage();
  }

  loadUserFromStorage(): void {
    console.log('📱 Loading user from localStorage...');
    const token = localStorage.getItem(this.tokenKey);
    const userId = localStorage.getItem(this.userIdKey);
    const userRole = localStorage.getItem(this.userRoleKey);

    console.log('🔍 Storage check:', { 
      hasToken: !!token, 
      userId, 
      userRole,
      tokenPreview: token ? token.substring(0, 20) + '...' : null
    });

    if (token && userId && userRole) {
      console.log('✅ User found in storage, creating session');
      // We don't have complete user info, but we can create a partial user object
      // The complete user info can be loaded when needed via getUserProfile()
      this.currentUserSubject.next({
        id: parseInt(userId),
        role: userRole as UserRole,
        firstName: '',
        lastName: '',
        email: '',
        enabled: true,
        createdAt: '',
        updatedAt: ''
      });
    } else {
      console.log('❌ No valid user session found');
    }
  }

  login(credentials: LoginRequest): Observable<any> {
    console.log('🔑 Attempting login for:', credentials.email);
    return this.apiService.post<any>('/auth/login', credentials).pipe(
      tap(response => {
        console.log('✅ Login successful, processing token...');
        this.handleAuthResponse(response);
      }),
      catchError(error => {
        console.error('❌ Login failed:', error);
        return throwError(() => new Error(error.error || 'Login failed. Please check your credentials.'));
      })
    );
  }

  registerStudent(registrationData: any): Observable<AuthResponse> {
    const studentData = {
      email: registrationData.email,
      password: registrationData.password,
      role: 'ETUDIANT',
      profile: {
        nom: registrationData.lastName,
        prenom: registrationData.firstName,
        telephone: registrationData.phoneNumber,
        schoolId: registrationData.schoolId,
        facultyId: registrationData.facultyId,
        departmentId: registrationData.departmentId,
        studyLevel: registrationData.studyLevel
      }
    };
    
    return this.apiService.post<AuthResponse>('/auth/register/student', studentData).pipe(
      tap(response => this.handleAuthResponse(response)),
      catchError(error => {
        console.error('Student registration error:', error);
        return throwError(() => new Error(error.error || 'Registration failed. Please try again.'));
      })
    );
  }

  registerCompany(registrationData: any): Observable<AuthResponse> {
    const companyData = {
      contactEmail: registrationData.contactEmail,
      password: registrationData.password,
      companyName: registrationData.companyName,
      companyIndustrySector: registrationData.companyIndustrySector
    };
    
    return this.apiService.post<AuthResponse>('/auth/register/company', companyData).pipe(
      tap(response => this.handleAuthResponse(response)),
      catchError(error => {
        console.error('Company registration error:', error);
        return throwError(() => new Error(error.error || 'Registration failed. Please try again.'));
      })
    );
  }

  registerTeacher(registrationData: any): Observable<AuthResponse> {
    const teacherData = {
      email: registrationData.contactEmail,
      password: registrationData.password,
      role: 'ENSEIGNANT',
      profile: {
        nom: registrationData.lastName,
        prenom: registrationData.firstName,
        telephone: registrationData.phoneNumber,
        schoolId: registrationData.schoolId === 'new' ? null : parseInt(registrationData.schoolId),
        facultyId: registrationData.facultyId === 'new' ? null : parseInt(registrationData.facultyId),
        departmentId: registrationData.departmentId === 'new' ? null : parseInt(registrationData.departmentId),
        newSchoolName: registrationData.newSchoolName,
        newFacultyName: registrationData.newFacultyName,
        newDepartmentName: registrationData.newDepartmentName
      }
    };
    
    console.log('📤 Sending teacher data:', teacherData);
    
    return this.apiService.post<AuthResponse>('/auth/register/teacher', teacherData).pipe(
      tap(response => this.handleAuthResponse(response)),
      catchError(error => {
        console.error('Teacher registration error:', error);
        return throwError(() => new Error(error.error || 'Registration failed. Please try again.'));
      })
    );
  }

  registerSchool(registrationData: any): Observable<AuthResponse> {
    console.log('🏫 School registration data received:', registrationData);
    
    // Validation des données requises
    if (!registrationData.contactEmail || !registrationData.password || !registrationData.schoolName) {
      return throwError(() => new Error('Champs obligatoires manquants'));
    }
    
    if (!registrationData.faculties || registrationData.faculties.length === 0) {
      return throwError(() => new Error('Au moins une faculté est requise'));
    }
    
    // Transformation des données pour le backend
    const schoolData = {
      contactEmail: registrationData.contactEmail,
      password: registrationData.password,
      schoolName: registrationData.schoolName,
      schoolAddress: registrationData.schoolAddress || '',
      schoolDescription: registrationData.schoolDescription || '',
      faculties: registrationData.faculties
    };
    
    console.log('📤 Sending school data to backend:', schoolData);
    
    return this.apiService.post<AuthResponse>('/auth/register/school', schoolData).pipe(
      tap(response => {
        console.log('✅ School registration successful:', response);
        this.handleAuthResponse(response);
      }),
      catchError(error => {
        console.error('❌ School registration error:', error);
        console.error('Error details:', error.error);
        const errorMsg = error.error?.message || error.message || 'Erreur lors de l\'inscription';
        return throwError(() => new Error(errorMsg));
      })
    );
  }

  getUserProfile(): Observable<User> {
    return this.apiService.get<User>('/users/me').pipe(
      tap(user => {
        // Update the stored user info
        this.currentUserSubject.next(user);
      }),
      catchError(error => {
        console.error('Get user profile error:', error);
        return throwError(() => new Error('Failed to load user profile.'));
      })
    );
  }

  logout(): void {
    console.log('🚪 Logging out user');
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userIdKey);
    localStorage.removeItem(this.userRoleKey);
    this.currentUserSubject.next(null);
    console.log('✅ Logout complete');
  }

  changePassword(currentPassword: string, newPassword: string): Observable<any> {
    return this.apiService.post('/users/change-password', {
      currentPassword,
      newPassword
    });
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  hasRole(role: UserRole): boolean {
    const userRole = localStorage.getItem(this.userRoleKey);
    console.log('🔍 Checking role:', { expected: role, actual: userRole });
    return userRole === role;
  }

  isStudent(): boolean {
    return this.hasRole(UserRole.STUDENT);
  }

  isCompany(): boolean {
    return this.hasRole(UserRole.COMPANY);
  }

  isFaculty(): boolean {
    return this.hasRole(UserRole.FACULTY);
  }

  isAdmin(): boolean {
    return this.hasRole(UserRole.ADMIN);
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  getCurrentUserId(): number | null {
    const userId = localStorage.getItem(this.userIdKey);
    return userId ? parseInt(userId) : null;
  }

  getCurrentUserRole(): UserRole | null {
    const role = localStorage.getItem(this.userRoleKey);
    return role as UserRole || null;
  }

  private decodeJWT(token: string): any {
    try {
      const payload = token.split('.')[1];
      const decoded = atob(payload);
      return JSON.parse(decoded);
    } catch (error) {
      console.error('❌ Error decoding JWT:', error);
      return null;
    }
  }

  private handleAuthResponse(response: any): void {
    console.log('🔍 Raw auth response:', response);
    
    // Extract token from response
    const token = response.token;
    if (!token) {
      throw new Error('No token received from server');
    }
    
    // Decode JWT to extract user info
    const decodedToken = this.decodeJWT(token);
    if (!decodedToken) {
      throw new Error('Invalid token received from server');
    }
    
    console.log('🔓 Decoded token:', decodedToken);
    
    const userId = decodedToken.userId;
    let role = decodedToken.role;
    
    // Map backend roles to frontend roles if needed
    if (role === 'ENSEIGNANT') {
      role = 'ENSEIGNANT'; // Keep as ENSEIGNANT for proper handling
    }
    
    console.log('💾 Saving auth data to localStorage:', {
      userId,
      role,
      tokenLength: token.length
    });
    
    // Save auth data to local storage
    localStorage.setItem(this.tokenKey, token);
    localStorage.setItem(this.userIdKey, userId.toString());
    localStorage.setItem(this.userRoleKey, role);
    
    // Update the current user subject
    this.loadUserFromStorage();
  }
}
