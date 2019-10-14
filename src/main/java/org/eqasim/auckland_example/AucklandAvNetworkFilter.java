package org.eqasim.auckland_example;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.opengis.feature.simple.SimpleFeature;

import ch.ethz.matsim.av.data.AVOperator;
import ch.ethz.matsim.av.network.AVNetworkFilter;

public class AucklandAvNetworkFilter implements AVNetworkFilter {
	private final GeometryFactory factory = new GeometryFactory();
	private final Collection<Geometry> shapes;

	public AucklandAvNetworkFilter(Collection<Geometry> shapes) {
		this.shapes = shapes;
	}

	@Override
	public boolean isAllowed(Id<AVOperator> operatorId, Link link) {
		for (Geometry shape : shapes) {
			Point point = factory.createPoint(new Coordinate(link.getCoord().getX(), link.getCoord().getY()));

			if (shape.contains(point)) {
				return true;
			}
		}

		return false;
	}

	static public AucklandAvNetworkFilter create(URL url) {
		Collection<Geometry> shapes = new LinkedList<>();

		try {
			DataStore dataStore = DataStoreFinder.getDataStore(Collections.singletonMap("url", url));

			SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
			SimpleFeatureCollection featureCollection = featureSource.getFeatures();
			SimpleFeatureIterator featureIterator = featureCollection.features();

			while (featureIterator.hasNext()) {
				SimpleFeature feature = featureIterator.next();
				shapes.add((Geometry) feature.getDefaultGeometry());
			}

			featureIterator.close();
			dataStore.dispose();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return new AucklandAvNetworkFilter(shapes);
	}
}
