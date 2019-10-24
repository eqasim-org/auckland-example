package org.eqasim.auckland_example.my_dispatcher;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.router.util.TravelTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ch.ethz.idsc.amodeus.dispatcher.core.RebalancingDispatcher;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.matsim.av.config.operator.OperatorConfig;
import ch.ethz.matsim.av.dispatcher.AVDispatcher;
import ch.ethz.matsim.av.framework.AVModule;
import ch.ethz.matsim.av.router.AVRouter;

public class MyDispatcher extends RebalancingDispatcher {
	private final Network network;

	public MyDispatcher(Config config, OperatorConfig operatorConfig, TravelTime travelTime, AVRouter router,
			EventsManager eventsManager, MatsimAmodeusDatabase db, Network network) {
		super(config, operatorConfig, travelTime, router, eventsManager, db);
		this.network = network;
	}

	@Override
	protected void redispatch(double now) {

	}

	public static class Factory implements AVDispatcherFactory {
		@Inject
		@Named(AVModule.AV_MODE)
		private TravelTime travelTime;

		@Inject
		private EventsManager eventsManager;

		@Inject
		private Config config;

		@Inject
		private MatsimAmodeusDatabase db;

		@Override
		public AVDispatcher createDispatcher(OperatorConfig operatorConfig, AVRouter router, Network network) {
			return new MyDispatcher(config, operatorConfig, travelTime, router, eventsManager, db, network);
		}
	}
}
