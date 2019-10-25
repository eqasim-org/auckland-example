package org.eqasim.auckland_example;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eqasim.auckland.AucklandModule;
import org.eqasim.auckland_example.simulation.AucklandAvModule;
import org.eqasim.auckland_example.simulation.AucklandDispatcherModule;
import org.eqasim.auckland_example.simulation.AucklandReferenceFrame;
import org.eqasim.automated_vehicles.components.AvConfigurator;
import org.eqasim.automated_vehicles.components.EqasimAvConfigGroup;
import org.eqasim.automated_vehicles.mode_choice.AvModeChoiceModule;
import org.eqasim.core.components.config.EqasimConfigGroup;
import org.eqasim.core.components.transit.EqasimTransitQSimModule;
import org.eqasim.core.simulation.EqasimConfigurator;
import org.eqasim.core.simulation.analysis.EqasimAnalysisModule;
import org.eqasim.core.simulation.calibration.CalibrationConfigGroup;
import org.eqasim.core.simulation.mode_choice.EqasimModeChoiceModule;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.dvrp.run.DvrpConfigGroup;
import org.matsim.contrib.dvrp.run.DvrpModule;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDatabaseModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusDispatcherModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVehicleGeneratorModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVehicleToVSGeneratorModule;
import ch.ethz.idsc.amodeus.matsim.mod.AmodeusVirtualNetworkModule;
import ch.ethz.idsc.amodeus.net.DatabaseModule;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.SimulationServer;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.framework.AVQSimModule;
import ch.ethz.matsim.av.routing.AVRoute;
import ch.ethz.matsim.av.routing.AVRouteFactory;
import ch.ethz.matsim.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;
import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;

public class RunSimulation {
	static public void main(String[] args) throws ConfigurationException, IOException, URISyntaxException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.allowOptions("config-path") //
				.allowPrefixes("mode-parameter", "cost-parameter") //
				.build();

		// If no specific path is provided, use the standard path in the project
		// directory
		String configPath = cmd.getOption("config-path").orElse("scenarios/auckland_1k/auckland_config.xml");

		// Set up what we want to read from the configuration file
		Config config = ConfigUtils.loadConfig(configPath, //
				new SwissRailRaptorConfigGroup(), //
				new EqasimConfigGroup(), //
				new DiscreteModeChoiceConfigGroup(), //
				new CalibrationConfigGroup(), //
				new AVConfigGroup(), //
				new DvrpConfigGroup(), //
				new EqasimAvConfigGroup());
		AvConfigurator.configure(config);
		cmd.applyConfiguration(config);

		// Here we can customize our configuration on the fly
		OperatorConfig operatorConfig = AVConfigGroup.getOrCreate(config)
				.getOperatorConfig(OperatorConfig.DEFAULT_OPERATOR_ID);

		// Set up how we want to load the scenario
		Scenario scenario = ScenarioUtils.createScenario(config);
		EqasimConfigurator.configureScenario(scenario);
		scenario.getPopulation().getFactory().getRouteFactories().setRouteFactory(AVRoute.class, new AVRouteFactory());
		ScenarioUtils.loadScenario(scenario);
		EqasimConfigurator.adjustScenario(scenario);

		// Set up the MATSim controller
		Controler controller = new Controler(scenario);
		EqasimConfigurator.configureController(controller);
		controller.addOverridingModule(new EqasimAnalysisModule());
		controller.addOverridingModule(new EqasimModeChoiceModule());
		controller.addOverridingModule(new AucklandModule(cmd));
		controller.addOverridingModule(new DvrpModule());
		controller.addOverridingModule(new AvModeChoiceModule(cmd));
		controller.addOverridingModule(new AucklandAvModule(cmd));

		// Set up Amodeus on top of MATSim + eqasim + av
		File workingDirectory = MultiFileTools.getDefaultWorkingDirectory();
		ScenarioOptions scenarioOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
		scenarioOptions.setProperty("virtualNetwork", "");
		scenarioOptions.setProperty("travelData", "");
		scenarioOptions.setProperty("LocationSpec", "AUCKLAND");
		Path absoluteConfigPath = Paths.get(config.getContext().toURI());
		System.out.println(absoluteConfigPath);
		Path workingDirectoryPath = FileSystems.getDefault().getPath(workingDirectory.getAbsolutePath());
		scenarioOptions.setProperty("simuConfig", workingDirectoryPath.relativize(absoluteConfigPath).toString());
		System.out.println(absoluteConfigPath.relativize(workingDirectoryPath).toString());
		System.out.println(workingDirectoryPath);
		System.out.println(workingDirectoryPath.relativize(absoluteConfigPath).toString());

       // System.exit(0);
		scenarioOptions.saveAndOverwriteAmodeusOptions();

		// Open server port for clients to connect to (e.g. viewer)
		SimulationServer.INSTANCE.startAcceptingNonBlocking();
		SimulationServer.INSTANCE.setWaitForClients(false);

		MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(scenario.getNetwork(),
				new AucklandReferenceFrame());

		controller.addOverridingModule(new AVModule(false));
		controller.addOverridingModule(new AmodeusModule());
		controller.addOverridingModule(new AmodeusDispatcherModule());
		controller.addOverridingModule(new AmodeusVehicleGeneratorModule());
		controller.addOverridingModule(new AmodeusVehicleToVSGeneratorModule());
		controller.addOverridingModule(new AmodeusDatabaseModule(db));
		controller.addOverridingModule(new AmodeusVirtualNetworkModule(scenarioOptions));
		controller.addOverridingModule(new DatabaseModule());
		controller.addOverridingModule(new AucklandDispatcherModule());

		// This is not totally obvious, but we need to adjust the QSim components if we
		// have AVs
		controller.configureQSimComponents(configurator -> {
			EqasimTransitQSimModule.configure(configurator);
			AVQSimModule.configureComponents(configurator);
		});
		
		config.controler().setOutputDirectory("output_500veh");
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(500);

		controller.run();
	}
}
