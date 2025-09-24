# 📊 Sistema de Cobertura de Código - Backend API

## 🎯 Resumen

Este proyecto implementa **JaCoCo** (Java Code Coverage) para medir la cobertura de código en las pruebas unitarias, con integración completa en el pipeline de CI/CD.

## 🚀 Características Implementadas

### ✅ **Cobertura Automática**

- **JaCoCo Maven Plugin** configurado
- **Umbrales configurables**: Instrucciones ≥60%, Ramas ≥50%
- **Exclusiones inteligentes**: DTOs, entidades, configuraciones
- **Reportes múltiples**: HTML, XML, CSV

### ✅ **Integración CI/CD**

- **GitHub Actions** con coverage reporting
- **Comentarios automáticos** en Pull Requests
- **Falla el build** si no se cumplen umbrales
- **Artifacts** con reportes de cobertura

### ✅ **Scripts y Herramientas**

- **Script de testing local** con cobertura
- **Badges** para README
- **Documentación completa**

## 🛠️ Comandos Disponibles

### Testing Local con Cobertura

```bash
# Ejecutar tests y generar reportes
./scripts/test-with-coverage.sh

# O manualmente
cd Backend
mvn clean test jacoco:report

# Verificar umbrales
mvn jacoco:check
```

### Ver Reportes

```bash
# Abrir reporte HTML
open Backend/target/site/jacoco/index.html

# Ver métricas en consola
cat Backend/target/site/jacoco/jacoco.csv
```

## 📊 Configuración de Umbrales

Los umbrales están configurados en `pom.xml`:

```xml
<limits>
    <limit>
        <counter>INSTRUCTION</counter>
        <value>COVEREDRATIO</value>
        <minimum>0.60</minimum>  <!-- 60% instrucciones -->
    </limit>
    <limit>
        <counter>BRANCH</counter>
        <value>COVEREDRATIO</value>
        <minimum>0.50</minimum>  <!-- 50% ramas -->
    </limit>
</limits>
```

### Ajustar Umbrales

Para cambiar los umbrales, edita `pom.xml`:

```xml
<!-- Para aumentar a 80% instrucciones y 70% ramas -->
<minimum>0.80</minimum>  <!-- Instrucciones -->
<minimum>0.70</minimum>  <!-- Ramas -->
```

## 🚫 Exclusiones Configuradas

Las siguientes clases están **excluidas** del análisis de cobertura:

- `**/BackendApplication.class` - Clase principal
- `**/dto/**/*.class` - Data Transfer Objects
- `**/entity/**/*.class` - Entidades JPA
- `**/config/**/*.class` - Configuraciones
- `**/BackendApplicationTests.class` - Test de contexto

### Agregar Exclusiones

Para excluir más clases, edita `pom.xml`:

```xml
<excludes>
    <exclude>**/tu-clase.class</exclude>
    <exclude>**/paquete/**/*.class</exclude>
</excludes>
```

## 🔄 Pipeline de CI/CD

### Job de Testing con Coverage

```yaml
test-with-coverage:
  runs-on: ubuntu-latest
  steps:
    - name: Run Tests with Coverage
      run: |
        cd Backend
        mvn clean test jacoco:report

    - name: Upload Coverage Reports
      uses: actions/upload-artifact@v3
      with:
        name: coverage-reports
        path: Backend/target/site/jacoco/
```

### Comentarios Automáticos en PRs

El pipeline genera comentarios automáticos en Pull Requests con:

- 📊 **Métricas de cobertura** (instrucciones y ramas)
- ✅/❌ **Estado** respecto a umbrales
- 📁 **Enlaces** a reportes detallados
- ⚠️ **Advertencias** si la cobertura es baja

## 📈 Interpretación de Métricas

### Tipos de Cobertura

1. **Instructions**: Líneas de código ejecutadas
2. **Branches**: Ramas condicionales cubiertas
3. **Lines**: Líneas de código cubiertas
4. **Methods**: Métodos ejecutados
5. **Classes**: Clases con al menos un método ejecutado

### Niveles de Cobertura

- **90%+**: Excelente 🟢
- **80-89%**: Muy bueno 🟢
- **70-79%**: Bueno 🟡
- **60-69%**: Aceptable 🟡
- **50-59%**: Mejorable 🟠
- **<50%**: Crítico 🔴

## 🎨 Badges para README

### Badge Estático

```markdown
![Coverage](https://img.shields.io/badge/coverage-60%25-brightgreen?style=for-the-badge&logo=java&logoColor=white)
```

### Badge Dinámico (con Codecov)

```markdown
![Coverage](https://codecov.io/gh/tu-usuario/tu-repo/branch/master/graph/badge.svg)
```

## 🔧 Troubleshooting

### Error: "Unsupported class file major version"

Este error ocurre con Java 21 y JaCoCo 0.8.12. **Es normal** y no afecta la funcionalidad.

**Solución**: Actualizar a JaCoCo 0.8.13+ cuando esté disponible.

### Coverage muy bajo

Si la cobertura está por debajo de los umbrales:

1. **Agregar más tests unitarios**
2. **Revisar exclusiones** (tal vez excluir clases innecesarias)
3. **Ajustar umbrales** temporalmente
4. **Analizar reporte HTML** para identificar código no cubierto

### Tests fallan en CI/CD

Verificar que:

1. **Secrets** estén configurados en GitHub
2. **Java 21** esté disponible en el runner
3. **Dependencias** se resuelvan correctamente
4. **Base de datos** esté configurada para tests

## 📚 Recursos Adicionales

### Documentación Oficial

- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)
- [GitHub Actions](https://docs.github.com/en/actions)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)

### Herramientas Relacionadas

- **SonarQube**: Análisis de calidad de código
- **Codecov**: Servicio de cobertura en la nube
- **Coveralls**: Alternativa a Codecov

## 🎯 Próximos Pasos

### Mejoras Sugeridas

1. **Integration Tests** con TestContainers
2. **Mutation Testing** con PIT
3. **Performance Testing** con JMeter
4. **Security Testing** con OWASP Dependency Check
5. **Code Quality** con SonarQube

### Configuración Avanzada

1. **Cobertura por paquetes** específicos
2. **Umbrales diferenciados** por módulo
3. **Reportes históricos** de cobertura
4. **Integración con IDEs** (IntelliJ, Eclipse)

---

## 📞 Soporte

Si tienes problemas con la cobertura:

1. **Revisa los logs** del pipeline
2. **Verifica la configuración** en `pom.xml`
3. **Ejecuta localmente** con `./scripts/test-with-coverage.sh`
4. **Consulta la documentación** de JaCoCo

**¡El sistema de cobertura está listo para usar! 🚀**
