import { Injectable } from '@angular/core';
import { CanActivate, CanActivateChild, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate, CanActivateChild {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.checkAuth(route);
  }

  canActivateChild(childRoute: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    return this.checkAuth(childRoute);
  }

  private checkAuth(route: ActivatedRouteSnapshot): boolean {
    // Check if user is logged in
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/auth/login'], { queryParams: { returnUrl: route.url.join('/') } });
      return false;
    }

    // If no roles are specified in the route data, just being logged in is enough
    const requiredRoles = route.data['roles'] as UserRole[];
    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    // Check if user has any of the required roles
    const userRole = localStorage.getItem('user_role') as UserRole;
    console.log('🔍 AuthGuard - User role:', userRole);
    console.log('🔍 AuthGuard - Required roles:', requiredRoles);
    
    if (!userRole) {
      this.router.navigate(['/unauthorized']);
      return false;
    }
    
    // Map backend roles to frontend roles
    const roleMapping: { [key: string]: UserRole } = {
      'ETUDIANT': UserRole.STUDENT,
      'ENTREPRISE': UserRole.COMPANY,
      'ENSEIGNANT': UserRole.FACULTY,
      'ADMIN': UserRole.ADMIN
    };
    
    const mappedRole = roleMapping[userRole] || userRole;
    console.log('🔍 AuthGuard - Mapped role:', mappedRole);
    
    if (!requiredRoles.includes(mappedRole as UserRole)) {
      console.log('❌ AuthGuard - Access denied');
      this.router.navigate(['/unauthorized']);
      return false;
    }

    console.log('✅ AuthGuard - Access granted');
    return true;
  }
}
