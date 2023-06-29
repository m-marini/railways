# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased] yyyy-mm-dd

### Added

- Issue #1: Add building blocks
- Issue #4: Immutable geometry, routes, trains
- Issue #15: Add sound signals
- Issue #21: Add double switch
- Issue #28: Simulator with rx4j
- Issue #43: Add train info panel
- Issue #45; Add mouse handling to panels
- Issue #47: Add game panel
- Issue #48: Add UI controller
- Issue #50: Add mouse event on map panel
- Issue #53: Compute distance from edges
- Issue #56: Add status change by user interaction
- Issue #58: Add entry/exit train counters
- Issue #60: Add end game and extended performance
- Issue #64: Add configuration file
- Issue #66: Add tool bar and menu
- Issue #65: Add game dialog
- Issue #67: Add user preferences dialog
- Issue #70: Add lock, stop actions
- Issue #71: Add autolock action
- Issue #74: Add dead end route and terminal platform
- Issue #81: Add Jackville stations

### Changed 

- Issue #2: Code for immutable objects
- Issue #3: Stop at platform for loading
- Issue #6: Lock signals when train changes section
- Issue #7: Approaching train state
- Issue #8: Braking train state
- Issue #9: Exiting train state
- Issue #16: Add drawing geometry
- Issue #18: Change section discovery to handle crossing points
- Issue #23: Add map panel
- Issue #25: Add station panel
- Issue #29: Add random arrival train
- Issue #34: Yaml Downville station
- Issue #39: Handling the cross of multiple edges in single step

### Fixed

- Issue #31: StationStatus null pointer
- Issue #36: Wrong curve drawing
- Issue #38: Trains hanging in DoubleSlipSwitch
- Issue #41: Train stops at central cross block
- Issue #62: Exiting train disappears from map panel
- Issue #61: Wrong signal lock at train crossing section
- Issue #83: Incoming train stuck up