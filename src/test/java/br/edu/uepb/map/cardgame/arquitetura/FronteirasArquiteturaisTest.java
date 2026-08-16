package br.edu.uepb.map.cardgame.arquitetura;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Regras executáveis para as fronteiras estruturais do framework.
 *
 * <p>O teste analisa o bytecode de produção. Por isso detecta dependências presentes
 * em herança, interfaces, campos, assinaturas, anotações e chamadas, mesmo quando não
 * existe um {@code import} explícito no código-fonte.
 *
 * @author Lucas Nóbrega de Araújo
 * @version 1.0
 */
@DisplayName("Fronteiras arquiteturais entre API, engine e clientes")
class FronteirasArquiteturaisTest {

    private static final String RAIZ_DO_PROJETO = "br.edu.uepb.map";
    private static final String PACOTE_API = "br.edu.uepb.map.cardgame.api..";
    private static final String PACOTE_ENGINE = "br.edu.uepb.map.cardgame.engine..";
    private static final String MOTOR =
            "br.edu.uepb.map.cardgame.engine.MotorDePartida";

    private static JavaClasses classesDeProducao;

    @BeforeAll
    static void importarBytecodeDeProducao() {
        classesDeProducao = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(RAIZ_DO_PROJETO);
    }

    @Test
    @DisplayName("API depende somente da biblioteca padrão e da própria API")
    void apiNaoDependeDoEngineNemDeJogosClientes() {
        noClasses()
                .that().resideInAPackage(PACOTE_API)
                .should().dependOnClassesThat()
                .resideOutsideOfPackages("java..", "javax..", PACOTE_API)
                .because("a API é a camada estável e não pode conhecer engine ou clientes")
                .check(classesDeProducao);
    }

    @Test
    @DisplayName("engine depende somente da biblioteca padrão, da API e dele mesmo")
    void engineNaoDependeDeJogosClientes() {
        noClasses()
                .that().resideInAPackage(PACOTE_ENGINE)
                .should().dependOnClassesThat()
                .resideOutsideOfPackages(
                        "java..", "javax..", PACOTE_API, PACOTE_ENGINE)
                .because("o runtime reutilizável não pode conhecer jogos concretos")
                .check(classesDeProducao);
    }

    @Test
    @DisplayName("MotorDePartida é o único tipo público do engine")
    void somenteMotorDePartidaEhPublicoNoEngine() {
        classes()
                .that().resideInAPackage(PACOTE_ENGINE)
                .and().doNotHaveFullyQualifiedName(MOTOR)
                .should().notBePublic()
                .because("os colaboradores do runtime são frozen-spots internos")
                .check(classesDeProducao);

        classes()
                .that().haveFullyQualifiedName(MOTOR)
                .should().bePublic()
                .because("os jogos clientes precisam estender o ponto de entrada do engine")
                .check(classesDeProducao);
    }
}
