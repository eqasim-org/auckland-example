package org.eqasim.auckland_example.preparation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eqasim.auckland_example.simulation.AucklandAvModeAvailability;
import org.eqasim.automated_vehicles.components.AvConfigurator;
import org.eqasim.core.simulation.EqasimConfigurator;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.ConfigWriter;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.FacilitiesWriter;
import org.matsim.pt.transitSchedule.api.TransitScheduleWriter;
import org.matsim.vehicles.VehicleWriterV1;

import ch.ethz.matsim.av.config.AVConfigGroup;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;

/**
 * Input path should be the path to the generated Auckland scenario from the
 * pipeline. Output path should be the path to a new directory where the final
 * scenario will be saved.
 */
public class PrepareScenario {
	static public void main(String[] args) throws ConfigurationException, IOException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("input-path", "output-path") //
				.build();

		Config config = ConfigUtils.loadConfig(
				new File(cmd.getOptionStrict("input-path"), "/auckland_config.xml").toString(),
				EqasimConfigurator.getConfigGroups());
		AvConfigurator.configure(config);
		cmd.applyConfiguration(config);

		AVConfigGroup.getOrCreate(config).setAllowedLinkMode("car");

		OperatorConfig operatorConfig = AVConfigGroup.getOrCreate(config)
				.getOperatorConfig(OperatorConfig.DEFAULT_OPERATOR_ID);
		operatorConfig.getGeneratorConfig().setNumberOfVehicles(100);
		operatorConfig.getDispatcherConfig().setType("DemandSupplyBalancingDispatcher");
		operatorConfig.getWaitingTimeConfig().setEstimationAlpha(0.1);
		operatorConfig.getWaitingTimeConfig().setEstimationLinkAttribute("avWaitingTimeGroup");
		operatorConfig.getWaitingTimeConfig().setEstimationInterval(3600.0);
		operatorConfig.getWaitingTimeConfig().setEstimationStartTime(4.0 * 3600.0);
		operatorConfig.getWaitingTimeConfig().setEstimationEndTime(23.0 * 3600.0);

		DiscreteModeChoiceConfigGroup dmcConfig = (DiscreteModeChoiceConfigGroup) config.getModules()
				.get(DiscreteModeChoiceConfigGroup.GROUP_NAME);
		dmcConfig.setModeAvailability(AucklandAvModeAvailability.NAME);

		// Operating area
		dmcConfig.getShapeFileConstraintConfigGroup().setPath("../operating_area/operating_area.shp");
		dmcConfig.getShapeFileConstraintConfigGroup().setConstrainedModes(Collections.singleton("av"));

		Set<String> constraints = new HashSet<>(dmcConfig.getTripConstraints());
		constraints.add("ShapeFile");
		dmcConfig.setTripConstraints(constraints);

		Scenario scenario = ScenarioUtils.createScenario(config);
		EqasimConfigurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);
		EqasimConfigurator.adjustScenario(scenario);

		AvConfigurator.configureCarLinks(scenario);
		AvConfigurator.configureWaitingTimeGroupFromShapefile(new File(cmd.getOptionStrict("input-path"), "/zones.shp"),
				"group", scenario.getNetwork());

		new ConfigWriter(config).write(new File(cmd.getOptionStrict("output-path"), "/auckland_config.xml").toString());
		new FacilitiesWriter(scenario.getActivityFacilities())
				.write(new File(cmd.getOptionStrict("output-path"), "/auckland_facilities.xml.gz").toString());
		new NetworkWriter(scenario.getNetwork())
				.write(new File(cmd.getOptionStrict("output-path") + "/auckland_network.xml.gz").toString());
		new PopulationWriter(scenario.getPopulation())
				.write(new File(cmd.getOptionStrict("output-path"), "/auckland_population.xml.gz").toString());
		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(
				new File(cmd.getOptionStrict("output-path"), "/auckland_transit_schedule.xml.gz").toString());
		new VehicleWriterV1(scenario.getTransitVehicles()).writeFile(
				new File(cmd.getOptionStrict("output-path"), "/auckland_transit_vehicles.xml.gz").toString());
	}
}
