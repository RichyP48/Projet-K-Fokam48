@echo off
echo "=== Test des utilisateurs par défaut ==="

echo "1. Test Admin (richardmogou99@gmail.com)"
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"richardmogou99@gmail.com\",\"password\":\"password\"}"

echo.
echo "2. Test Étudiant (johanmogou@gmail.com)"
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"johanmogou@gmail.com\",\"password\":\"password\"}"

echo.
echo "3. Test Enseignant (sarahmogou@gmail.com)"
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"sarahmogou@gmail.com\",\"password\":\"password\"}"

echo.
echo "4. Test Entreprise (researchecenter@pkf.com)"
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"researchecenter@pkf.com\",\"password\":\"password\"}"

echo.
echo "=== Tests terminés ==="