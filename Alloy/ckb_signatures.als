open util/boolean

abstract sig User {}

enum Status { Open, Closed }

sig Student extends User {
	var badges: set Badge,
	// var score: one Int,
	var elegibleForBadges: set Badge
} {
	// score >= 0
}

sig Educator extends User {
	owns: disj set Tournament,
	hasPermissionsFor: set Tournament 
}

sig Battle {
	groupsMinSize: one Int,
	groupsMaxSize: one Int,
	requiresManualEvaluation: Bool,
	var status: one Status,
	enrolledGroups: disj set Group
} {
	groupsMinSize > 0 and 
	groupsMaxSize >= groupsMinSize and 
	groupsMaxSize <= 2 // for simulation purposes to limit number of signatures
}

sig Tournament {
	hosts: disj set Battle,
	hasBadges: disj set Badge, 
	var status: one Status 
}

sig Badge {}

var sig Solution {
	var evaluated: Bool,
	var evaluatedBy: lone Educator
}

sig Group {
	owner: one Student,
	members: some Student,
	// var battleScore: one Int,
	var uploadedSolution: disj lone Solution
} {
	// battleScore >= 0
}