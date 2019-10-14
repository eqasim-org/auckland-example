package org.eqasim.auckland_example;

import org.eqasim.auckland_example.my_dispatcher.MyDispatcher;
import org.matsim.core.controler.AbstractModule;

import ch.ethz.matsim.av.framework.AVUtils;

public class AucklandDispatcherModule extends AbstractModule {
	@Override
	public void install() {
		AVUtils.registerDispatcherFactory(binder(), MyDispatcher.class.getSimpleName(), MyDispatcher.Factory.class);
	}
}
