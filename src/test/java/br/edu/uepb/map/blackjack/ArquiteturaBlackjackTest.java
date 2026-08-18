package br.edu.uepb.map.blackjack;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import br.edu.uepb.map.cardgame.engine.MotorDePartida;

/** Regras executáveis para a fronteira entre o cliente Blackjack e o framework. */
@DisplayName("Fronteira arquitetural do cliente Blackjack")
class ArquiteturaBlackjackTest {

    private static final String PACOTE_BLACKJACK = "br.edu.uepb.map.blackjack..";
    private static final String PACOTE_API = "br.edu.uepb.map.cardgame.api..";
    private static final String PACOTE_ENGINE = "br.edu.uepb.map.cardgame.engine..";
    private static JavaClasses classesDeProducao;

    @BeforeAll
    static void importarProducao() {
        classesDeProducao = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("br.edu.uepb.map");
    }

    @Test
    @DisplayName("cliente depende apenas de Java, da API, do motor público e dele mesmo")
    void deveRespeitarAsDependenciasPermitidas() {
        classes()
                .that().resideInAPackage(PACOTE_BLACKJACK)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "java..", "javax..", PACOTE_API, PACOTE_ENGINE, PACOTE_BLACKJACK)
                .because("um jogo cliente não deve depender de outro jogo ou biblioteca acidental")
                .check(classesDeProducao);

        var colaboradoresInternos = JavaClass.Predicates
                .resideInAPackage(PACOTE_ENGINE)
                .and(JavaClass.Predicates.equivalentTo(MotorDePartida.class).negate());
        noClasses()
                .that().resideInAPackage(PACOTE_BLACKJACK)
                .should().dependOnClassesThat(colaboradoresInternos)
                .because("MotorDePartida é o único ponto público permitido do engine")
                .check(classesDeProducao);
    }
}
