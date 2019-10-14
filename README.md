# Automated Mobility on Demand (AMoD) in Auckland

This repository contains an example simulation of Automated Mobility
on Demand in Auckland. The simulation uses the agent- and activity-based
transport simulation framework [MATSim](http://www.matsim.org). It furthermore
demonstrates the interplay of two novel extensions of MATSim.

The first is the new [Discrete Mode Choice Extension](https://github.com/matsim-eth/discrete-mode-choice)
which allows to simulate detailed and configurable dynamic traveller choice behaviour. The second
is [AMoDeus](https://amodeus.science/), a simulator of dynamic transport services such as taxis
and Mobility on Demand. 

While this repository provides an example of how to set up a simulation with those tools it also
comes with all data needed for a basic transport scenario for Auckland, New Zealand, which is 
based on the [eqasim](http://eqasim.org/) transport simulation pipeline.

The simulations in this repository try to explore how different fleet operating policies interact
with traveller preferences. To learn more about the research question and examples from our 
ongoing studies, make sure to slide through our [workshop presentation from ITSC 2019](https://slides.com/sebastianhorl/itsc19). 

## Running the simulation 5 minutes

To run a basic simulation you'll need either [Eclipse](https://www.eclipse.org/downloads/) or IntelliJ and you need a working
[Java JDK](https://adoptopenjdk.net/). 

Once those tools are set up, you can clone this *git* repository and import it as a Maven project (both "Import project" in Eclipse). You'll find a Java class called `RunSimulation`. This is the run script that you can start. For that, right click on the class and choose "Run As > Java Application". Et voilà, your first AMoD simulation of Auckland is running.

You will see that things are happening on the console in Eclipse and that there is a new `simulation_output` folder in the project directory (usually where you have cloned this repository). It contains a number of standard output files. For instance, check `simulation_output/modestats.png`. You will see how the mode share in the scenario changes. At first, only few people use the `av` mode, but after a couple of iterations you will see how more and more travellers are attracted to the service.

Still, just looking at mode shares is a bit boring. Keep reading!

## Visualizing what is happening

To visualize what is happening inside of the simulation you will need a viewer that is developed by the Institute für Dynamic Systems and Control and ETH Zurich. To get it, visit the [repository website](https://github.com/idsc-frazzoli/amod). Simply clone and import the [amod](https://github.com/idsc-frazzoli/amod) project as you did before. Then, find the `ScenarioViewer` class, right click on it and go to "Run As > Run Configurations ...". Choose "ScenarioViewer" in the list on the right if it is not already selected and go to the "Arguments" tab. There go down to "Working Directory" and set the project path of the other repository as the working directory (it is the one that now contains `simulation_output`, remember?). 

If everything goes well, a GUI window should show up with a map of Auckland. Don't worry if you see any error messages in the background. This is about stuff that we do not use in our simple example here. Now click "Connect" and you should be able to see what is currently happening in the simulation! You can see where idle AMoD vehicles are, which ones are currently carrying customers, going empty and which vehicles are assigned to which customer at the moment. This tool allows you to observe in detail what specific fleet control algorithms are doing.

## Some first experiments

With these tools at hand you can already start playing around with the simulation. Here are a couple of hints, but all of the details you will find in the repositories of [MATSim](http://www.matsim.org), [eqasim](http://eqasim.org/), and [AMoDeus](https://amodeus.science/).

### Experiment 1: Choosing a different operating area

You may notice that currently AMoD vehicles are serving customers everywhere in the study area. You can change this! In your project repository check out `scenarios/operating_area/operating_area.shp`. This file is a shape file, which is the de facto standard in GIS for defining spatial information. There are various tools to work with that file, we recommend [QGIS](https://qgis.org). In QGIS, you can load the file by "adding a Vector Layer". Things get easier if you install the "QuickMapServices" plugin in QGIS. It will allow you to go to "Web" in the top menu and add a "OSM Standard" layer in the background. You can then use QGIS to modify `operating_area.shp`. Alternatively, save a new shape file to a different path and adjust `scenarios/auckland_1k/auckland_config.xml`. This file contains all relevant configuration options for your simulation. Search for `tripConstraint:ShapeFile` and adjust the `path` parameter a few lines below to your new path.

What do you observe if you define operating areas of different size?

### Experiment 2: Different fleet size

In your scenario config file (`scenarios/auckland_1k/auckland_config.xml`) you can also define the fleet size for the service. Search for `numberOfVehicles` and adjust the number in the file. Then restart the `RunSimulation` class to see what happens. In the background, thanks to eqasim and the Discrete Mode Choice extension, all agents make decision from iteration to iteration. Their choices are defined by a choice model, which depends on various parameters. One strong influence that may make your AMoD service more or less attractive compared to the other modes of transport is the service level, i.e. the generated waiting times. Changing the size of your fleet directly changes how many waiting time agents experience. Since they try to adapt to what the supply side offers, less agents will use your service if fleet sizes become to small. 

What do you observe if you set your fleet size very low or very high? Can you find a fleet size where you attract the maximum number of travellers?

### Experiment 3: Cost structure

The second major component of how agents value your service is price. In this simulation, a simple cost model has been implemented for the operator. You can define the cost *per vehicle and day* and the *cost per driven kilometer*. You can then define how much of the total fleet cost the operator passes on to the customers. A percentage of 120% would mean that the operator has a profit margin (or additional fees to pay) of 20%. A value of 60% could mean that the service is subsidized by 40%. 

Cost parameters can be changed via command line argments. For that, go to "Run Configurations" in Eclipse and go to the "Arguments" tab of your `RunSimulation` configuration. There, you can add command line arguments, like so:

```
--cost-parameter:vehicleCost_MU 63.0
--cost-parameter:distanceCost_MU_km 0.64
--cost-parameter:priceFactor 1.0
```

These numbers are the standard values of the simulation. Feel free to play around with them. Can you observe how the demand changes if you offer lower or higher prices? You can also observe how the price changes while the systems goes into equilibirum by looking at `simulation_output/av_prices.csv`.

### Experiment 4: Use a different dispatcher

While price and waiting times are important the easiest way to keep them under control is by defining en detail what each vehicle of the fleet is supposed to do throughout the day. This is the task of an algorithm called *operating policy* or *dispatcher*. AMoDeus provides a large number of operating polciies that have been presented in literature, both for single-passenger services and ride-pooling. While many dispatchers can be run without further work, you need to set up GLPK for the more advanced ones. If you're interested, this is explained in [amod](https://github.com/idsc-frazzoli/amod).

For now, you can go to your `auckland_config.xml` and find `<parameterset type="dispatcher" >`. In the line below you can choose the dispatcher *type*. Try for instance the following ones:

- *DemandSupplyBalancingDispatcher* is a heuristic approach that tries to offer good service at peak times while distributing waiting times more equally at off-peak times
- *GlobalBipartiteMatchingDispatcher* is the classic *distance minimization policy*. It tries to let the fleet drive as little as possible. By that, it may cause long waiting times in remote areas.
- *ExtDemandSupplyBeamSharing* is a simple pooling dispatcher for requests that have similar start and end points

Do you see differences when using those dispatchers? Both in macroscopic and detailed analysis in the viewer? What do you observe about run time when using the simple heuristic, vs. a more complicated single-unit dispatcher vs. a pooling dispatcher?

By the way, if you want to see the dispatchers in action with more agents, you can use a larger scenario. So far you were using a version of Auckland with only 1,000 agents. This repository also contains a version with 10,000 agents. To use this scenario, adjust the upper part of `RunSimulation` to use the files from `auckland_10k` instead of `auckland_1k` or provide the path to the config file via command line arguments:

```
--config-path /path/to/my/scenarios/auckland_10k/auckland_config.xml
```

## Your own dispatcher

While it is fun to try out all of these different things, you may have come to this repository to do some programming and fleet control on your own. To do that, choose the `MyDispatcher` in `auckland_config.xml`. Then, check out the `my_dispatcher` package of this repository in Eclipse. The `MyDispatcher` class is an empty operating policy that does nothing at all for now. It is your task to fill it with dispatching commands for your vehicles. To learn how to do that check out the documentation of AMoDeus or follow our workshop :)

## How can I learn more?

To learn more about all that has been shown here, visit the websites of [MATSim](http://www.matsim.org), [eqasim](http://eqasim.org/), and [AMoDeus](https://amodeus.science/). Also, have a look at some of or recent papers that make use of the mentioned choice models and control strategies:

- Ruch, C., S. Hörl and E. Frazzoli (2018) [Amodeus, a simulation-based testbed for autonomous mobility-on-demand systems](https://ieeexplore.ieee.org/document/8569961), paper presented at the 21th IEEE Conference on Intelligent Transportation Systems.
- Hörl, S., C. Ruch, F. Becker, E. Frazzoli and K.W. Axhausen (2019) [Fleet operational policies for automated mobility: A simulation assessment for Zurich](https://www.sciencedirect.com/science/article/pii/S0968090X18304248), *Transportation Research Part C: Emerging Technologies*, **102**, 20-31.
- Hörl, S., M. Balac and K.W. Axhausen (2019) [Dynamic demand estimation for an AMoD system in Paris](https://ieeexplore.ieee.org/document/8814051), paper presented at the 30th IEEE Intelligent Vehicles Symposium, June 2019, Paris, France.
- Hörl, S., M. Balac and K.W. Axhausen (2019) [Pairing discrete mode choice models and agent-based transport simulation with MATSim](https://www.research-collection.ethz.ch/handle/20.500.11850/303667), presented at the 98th Annual Meeting of the Transportation Research Board, January 2019, Washington D.C.




