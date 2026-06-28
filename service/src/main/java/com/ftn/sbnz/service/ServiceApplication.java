package com.ftn.sbnz.service;

import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class ServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

	@Bean
	public KieContainer kieContainer() {
		KieServices ks = KieServices.Factory.get();

		ReleaseId releaseId = ks.newReleaseId("com.ftn.sbnz", "kjar", "0.0.1-SNAPSHOT");
		KieContainer originalContainer = ks.newKieContainer(releaseId);

		// new KieFileSystem containing all rules (original and template generated rules)
		KieFileSystem kfs = ks.newKieFileSystem();
		ExternalSpreadsheetCompiler converter = new ExternalSpreadsheetCompiler();

		String[] originalDrls = {
                "rules/forwardchaining/fc-level-1-driver.drl",
                "rules/forwardchaining/fc-level-2-vehicle.drl",
                "rules/forwardchaining/fc-level-3-permits.drl",
                "rules/forwardchaining/fc-level-4-decision.drl",
				"rules/cep/cep-alarms.drl",
		};
		for (String path : originalDrls) {
			try (InputStream is = originalContainer.getClassLoader().getResourceAsStream(path)) {
				if (is != null) {
					kfs.write("src/main/resources/" + path, new String(is.readAllBytes(), StandardCharsets.UTF_8));
				}
			} catch (Exception ignored) {
			}
		}

		compileTemplate(kfs, converter, originalContainer, "rules/templates/driver-qualification.drt", "rules/templates/driver-qualification-data.xls", "src/main/resources/rules/templates/driver.drl");

		kfs.writeKModuleXML(
				"<kmodule xmlns='http://jboss.org/kie/6.0.0/kmodule'>" +
						"  <kbase name='forwardKBase' packages='rules.forwardchaining, rules.templates' >" +
						"    <ksession name='forwardKSession'/>" +
						"  </kbase>" +
						"  <kbase name='cepKBase' packages='rules.cep' eventProcessingMode='stream'>" +
						"    <ksession name='cepKSession' type='stateful' clockType='realtime'/>" +
						"  </kbase>" +
						"</kmodule>"
		);
		kfs.generateAndWritePomXML(releaseId);

		KieBuilder kb = ks.newKieBuilder(kfs);
		kb.buildAll();

		if (!kb.getResults().getMessages(org.kie.api.builder.Message.Level.ERROR).isEmpty()) {
			KieScanner kScanner = ks.newKieScanner(originalContainer);
			kScanner.start(1000);
			return originalContainer;
		}

		ks.getRepository().addKieModule(kb.getKieModule());
		KieContainer finalContainer = ks.newKieContainer(releaseId);
		KieScanner kScanner = ks.newKieScanner(finalContainer);
		kScanner.start(1000);
		return finalContainer;
	}


	private void compileTemplate(KieFileSystem kfs, ExternalSpreadsheetCompiler converter,
								 KieContainer container, String templatePath, String dataPath, String outputPath) {
		try (InputStream t = container.getClassLoader().getResourceAsStream(templatePath);
			 InputStream d = container.getClassLoader().getResourceAsStream(dataPath)) {
			if (t != null && d != null) {
				kfs.write(outputPath, converter.compile(d, t, 2, 1));
			}
		} catch (Exception ignored) {}
	}

}