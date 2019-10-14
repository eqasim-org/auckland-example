package org.eqasim.auckland_example;

import java.io.File;

import org.eqasim.auckland.costs.AucklandCostParameters;
import org.eqasim.automated_vehicles.components.EqasimAvConfigGroup;
import org.eqasim.automated_vehicles.mode_choice.cost.AvCostParameters;
import org.eqasim.automated_vehicles.mode_choice.mode_parameters.AvModeParameters;
import org.eqasim.core.simulation.mode_choice.AbstractEqasimExtension;
import org.eqasim.core.simulation.mode_choice.ParameterDefinition;
import org.eqasim.core.simulation.mode_choice.parameters.ModeParameters;
import org.matsim.core.config.CommandLine;
import org.matsim.core.utils.io.IOUtils;

import com.google.inject.Provides;
import com.google.inject.Singleton;

import ch.ethz.matsim.av.network.AVNetworkFilter;
import ch.ethz.matsim.av.network.NullNetworkFilter;
import ch.ethz.matsim.discrete_mode_choice.modules.config.DiscreteModeChoiceConfigGroup;

public class AucklandAvModule extends AbstractEqasimExtension {
	private final CommandLine commandLine;

	public AucklandAvModule(CommandLine commandLine) {
		this.commandLine = commandLine;
	}

	@Override
	protected void installEqasimExtension() {
		bindModeAvailability(AucklandAvModeAvailability.NAME).to(AucklandAvModeAvailability.class);
	}

	@Provides
	@Singleton
	public AucklandAvCostParameters provideAucklandAvCostParameters(EqasimAvConfigGroup config) {
		AucklandAvCostParameters parameters = AucklandAvCostParameters.buildDefault();

		if (config.getCostParametersPath() != null) {
			ParameterDefinition.applyFile(new File(config.getCostParametersPath()), parameters);
		}

		ParameterDefinition.applyCommandLine("cost-parameter", commandLine, parameters);
		return parameters;
	}

	@Provides
	@Singleton
	public AvCostParameters provideAvCostParameters(AucklandAvCostParameters parameters) {
		return parameters.av;
	}

	@Provides
	@Singleton
	public AucklandCostParameters provideCostParameters(AucklandAvCostParameters parameters) {
		return parameters;
	}

	@Provides
	@Singleton
	public AucklandAvModeParameters provideAucklandAvModeParameters(EqasimAvConfigGroup config) {
		AucklandAvModeParameters parameters = AucklandAvModeParameters.buildDefault();

		if (config.getCostParametersPath() != null) {
			ParameterDefinition.applyFile(new File(config.getCostParametersPath()), parameters);
		}

		ParameterDefinition.applyCommandLine("mode-parameter", commandLine, parameters);
		return parameters;
	}

	@Provides
	@Singleton
	public AvModeParameters provideAvModeParameters(AucklandAvModeParameters parameters) {
		return parameters.av;
	}

	@Provides
	@Singleton
	public ModeParameters provideModeParameters(AucklandAvModeParameters parameters) {
		return parameters;
	}

	@Provides
	@Singleton
	public AVNetworkFilter provideAVNetworkFilter(DiscreteModeChoiceConfigGroup dmcConfig) {
		if (dmcConfig.getTripConstraints().contains("ShapeFile")
				&& dmcConfig.getShapeFileConstraintConfigGroup().getPath() != null) {
			return AucklandAvNetworkFilter.create(
					IOUtils.newUrl(getConfig().getContext(), dmcConfig.getShapeFileConstraintConfigGroup().getPath()));
		} else {
			return new NullNetworkFilter();
		}
	}
}
