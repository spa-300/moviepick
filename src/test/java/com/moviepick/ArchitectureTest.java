package com.moviepick;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

// 패키지 의존 방향 규칙. 운영 코드만 검사한다 (테스트 코드는 제외)
@AnalyzeClasses(packages = "com.moviepick", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // 엔티티 패키지의 enum(MemberStatus 등)은 관리자 요청 값으로 쓸 수 있다
    private static final DescribedPredicate<JavaClass> ENUMS =
            DescribedPredicate.describe("enum", JavaClass::isEnum);

    @ArchTest
    static final ArchRule global은_domain에_의존하지_않는다 =
            noClasses().that().resideInAPackage("com.moviepick.global..")
                    .should().dependOnClassesThat().resideInAPackage("com.moviepick.domain..");

    @ArchTest
    static final ArchRule domain_패키지끼리_순환이_없다 =
            slices().matching("com.moviepick.domain.(*)..")
                    .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule admin은_repository와_entity를_직접_쓰지_않는다 =
            noClasses().that().resideInAPackage("com.moviepick.domain.admin..")
                    .should().dependOnClassesThat(
                            resideInAnyPackage("com.moviepick..repository..", "com.moviepick..entity..")
                                    .and(DescribedPredicate.not(ENUMS))
                    );
}
