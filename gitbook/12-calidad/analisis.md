# Análisis estático

## Calidad y revisión de cierre

Revisión real del 6 de octubre de 2026. El PDF oficial UMG, página 2, exige entrega en **Sonat (https://sonat.com/)**. No menciona SonarQube, SonarCloud o SonarLint. Sonat es la plataforma de documentación y entrega; instalar SonarQube no demuestra un envío académico.

## Herramientas y resultados

Apache Maven PMD Plugin **3.28.0**, PMD/CPD **7.17.0**, reglas predeterminadas, Java 17, todas las fuentes Java de producción, sin exclusiones. Se ejecutaron `pmd` y `cpd`, no `pmd:check` ni una Quality Gate Sonar. La salida BUILD SUCCESS acredita que el análisis terminó, no que no haya advertencias.

| Medida | Resultado real |
|---|---|
| Advertencias iniciales PMD | 10 |
| Advertencias finales PMD | 5 |
| Errores de procesamiento | 0 |
| Hallazgos prioridad 1 o 2 en reglas ejecutadas | 0 |
| Grupos de duplicación CPD, umbral predeterminado 100 tokens | 1 |
| Bugs/vulnerabilidades según Sonar | No medidos: no hubo análisis Sonar |
| Complejidad | Sin métrica calculada con este conjunto de reglas |
| Quality Gate Sonar | No ejecutada; no exigida por la fuente oficial |

Las advertencias PMD no se convierten automáticamente en bugs, vulnerabilidades o code smells de Sonar. La ausencia de hallazgos de prioridad alta no certifica seguridad exhaustiva.

## Correcciones pequeñas realizadas

Tres pruebas/utilidades JDBC leían el resultado sin comprobar `next()`: `PruebasConexion`, `PruebaConexion2` y `VerificarBases`. Ahora rechazan una respuesta sin filas con SQLException. `ConexionRecurso` registra internamente el tipo de fallo sin publicar stack, SQL ni secretos. También se separó su comprobación de fila de la lectura; el cortocircuito anterior era correcto, pero PMD lo señalaba como CheckResultSet. Se conservan ambos informes para distinguir un problema real de una advertencia del analizador.

Se repitieron compilación y pruebas HTTP después de los cambios. WPF compiló sin advertencias ni errores y su self-test se ejecutó contra el backend desplegado.

## Advertencias conservadas

| Archivo | Línea | Regla | Prioridad |
|---|---|---|---|
| `src/main/java/com/adquisiciones/dao/SqlDAO.java` | 9 | UnnecessaryFullyQualifiedName | 4 |
| `src/main/java/com/adquisiciones/dao/SqlDAO.java` | 12 | UnnecessaryFullyQualifiedName | 4 |
| `src/main/java/com/adquisiciones/filtro/AuthFilter.java` | 37 | CollapsibleIfStatements | 3 |
| `src/main/java/com/adquisiciones/util/SemillaDemo.java` | 123 | UselessParentheses | 4 |
| `src/main/java/com/adquisiciones/util/SemillaDemo.java` | 136 | UnnecessaryFullyQualifiedName | 4 |

Son recomendaciones de calificación de nombres, paréntesis y combinación de condiciones; se dejan documentadas para evitar un refactor de estilo al final. CPD detectó 1 grupo(s): [{'lineas': 17, 'tokens': 101, 'ocurrencias': 3}]. Deben valorarse como deuda de mantenimiento, no como prueba de fallo funcional.

## Revisión dirigida

- Java: PreparedStatement en DAO y filtros; nombres de tablas/columnas proceden de Modulo estático; try-with-resources; transacciones SERIALIZABLE con rollback; autorización en filtro/servicios y validaciones temporales.
- Web: construcción de DOM con textContent, mensajes de API, CSRF, filtros y paginación. `node --check` se ejecutó sobre los tres JavaScript del sistema. Se preservan páginas originales.
- WPF: HttpClient, mensajes de validación, cookies/CSRF, viewmodels de editor y flujo; ordenamiento numérico y formato decimal probados. El self-test no certifica cada clic nativo.

## Reproducir y consultar evidencia

```powershell
./scripts/calidad.ps1 -Maven mvn
```

Los XML reales `antes-pmd.xml`, `despues-pmd.xml`, `antes-cpd.xml` y `despues-cpd.xml`, y `resumen.json`, están en `docs/evidencias/calidad/`. PMD es local y no envía código a un servicio externo. [Uso oficial Apache](https://maven.apache.org/plugins/maven-pmd-plugin/usage.html).

## Sonat y GitBook

No hay sesión académica Sonat ni confirmación de envío. En GitBook se observó la pantalla de inicio de sesión del espacio solicitado. La documentación se prepara para sincronización; la publicación externa no se declara realizada.


[Volver a Calidad del software](README.md)
