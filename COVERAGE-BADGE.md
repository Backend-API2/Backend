# 📊 Coverage Badge para README

## 🏷️ Badge de Cobertura de Código

Para agregar un badge de cobertura a tu README.md, usa este código:

```markdown
![Coverage](https://img.shields.io/badge/coverage-60%25-brightgreen?style=for-the-badge&logo=java&logoColor=white)
```

## 🎯 Badges Disponibles

### Badge Básico

```markdown
![Code Coverage](https://img.shields.io/badge/coverage-60%25-brightgreen)
```

### Badge con Logo de Java

```markdown
![Java Coverage](https://img.shields.io/badge/coverage-60%25-brightgreen?style=for-the-badge&logo=java&logoColor=white)
```

### Badge Dinámico (requiere integración con servicios externos)

```markdown
![Coverage](https://codecov.io/gh/tu-usuario/tu-repo/branch/master/graph/badge.svg)
```

## 🎨 Colores Disponibles

- `brightgreen` - ≥80% (Excelente)
- `green` - ≥70% (Bueno)
- `yellowgreen` - ≥60% (Aceptable)
- `yellow` - ≥50% (Mejorable)
- `orange` - ≥40% (Bajo)
- `red` - <40% (Crítico)

## 📋 Ejemplo de README.md

````markdown
# 🚀 Backend API

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen?style=for-the-badge&logo=springboot&logoColor=white)
![Coverage](https://img.shields.io/badge/coverage-60%25-brightgreen?style=for-the-badge&logo=java&logoColor=white)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen?style=for-the-badge)

## 📊 Métricas de Calidad

- **Cobertura de Código**: 60%+ (Instrucciones), 50%+ (Ramas)
- **Tests**: Unitarios con JUnit 5
- **CI/CD**: GitHub Actions con deployment automático
- **Monitoreo**: Health checks múltiples

## 🧪 Testing

```bash
# Ejecutar tests con cobertura
./scripts/test-with-coverage.sh

# Ver reporte HTML
open Backend/target/site/jacoco/index.html
```
````

```

## 🔄 Actualización Automática

Para badges dinámicos que se actualicen automáticamente, puedes integrar con:

1. **Codecov** - Servicio gratuito para cobertura
2. **Coveralls** - Alternativa a Codecov
3. **SonarCloud** - Análisis de calidad completo

### Configuración con Codecov

1. Registrarse en [codecov.io](https://codecov.io)
2. Conectar tu repositorio de GitHub
3. Agregar el badge dinámico al README
4. El badge se actualizará automáticamente con cada push
```
