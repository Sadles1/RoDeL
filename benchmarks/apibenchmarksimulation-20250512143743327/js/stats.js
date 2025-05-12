var stats = {
    type: "GROUP",
name: "All Requests",
path: "",
pathFormatted: "group_missing-name--1146707516",
stats: {
    "name": "All Requests",
    "numberOfRequests": {
        "total": "659",
        "ok": "657",
        "ko": "2"
    },
    "minResponseTime": {
        "total": "5",
        "ok": "5",
        "ko": "10"
    },
    "maxResponseTime": {
        "total": "330",
        "ok": "330",
        "ko": "204"
    },
    "meanResponseTime": {
        "total": "90",
        "ok": "90",
        "ko": "107"
    },
    "standardDeviation": {
        "total": "93",
        "ok": "93",
        "ko": "97"
    },
    "percentiles1": {
        "total": "50",
        "ok": "50",
        "ko": "107"
    },
    "percentiles2": {
        "total": "162",
        "ok": "161",
        "ko": "156"
    },
    "percentiles3": {
        "total": "266",
        "ok": "266",
        "ko": "194"
    },
    "percentiles4": {
        "total": "315",
        "ok": "315",
        "ko": "202"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 657,
    "percentage": 99.69650986342944
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 2,
    "percentage": 0.30349013657056145
},
    "meanNumberOfRequestsPerSecond": {
        "total": "17.34",
        "ok": "17.29",
        "ko": "0.05"
    }
},
contents: {
"req_signed-request-231114123": {
        type: "REQUEST",
        name: "Signed Request",
path: "Signed Request",
pathFormatted: "req_signed-request-231114123",
stats: {
    "name": "Signed Request",
    "numberOfRequests": {
        "total": "659",
        "ok": "657",
        "ko": "2"
    },
    "minResponseTime": {
        "total": "5",
        "ok": "5",
        "ko": "10"
    },
    "maxResponseTime": {
        "total": "330",
        "ok": "330",
        "ko": "204"
    },
    "meanResponseTime": {
        "total": "90",
        "ok": "90",
        "ko": "107"
    },
    "standardDeviation": {
        "total": "93",
        "ok": "93",
        "ko": "97"
    },
    "percentiles1": {
        "total": "50",
        "ok": "50",
        "ko": "107"
    },
    "percentiles2": {
        "total": "162",
        "ok": "161",
        "ko": "156"
    },
    "percentiles3": {
        "total": "266",
        "ok": "266",
        "ko": "194"
    },
    "percentiles4": {
        "total": "315",
        "ok": "315",
        "ko": "202"
    },
    "group1": {
    "name": "t < 800 ms",
    "htmlName": "t < 800 ms",
    "count": 657,
    "percentage": 99.69650986342944
},
    "group2": {
    "name": "800 ms <= t < 1200 ms",
    "htmlName": "t >= 800 ms <br> t < 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group3": {
    "name": "t >= 1200 ms",
    "htmlName": "t >= 1200 ms",
    "count": 0,
    "percentage": 0.0
},
    "group4": {
    "name": "failed",
    "htmlName": "failed",
    "count": 2,
    "percentage": 0.30349013657056145
},
    "meanNumberOfRequestsPerSecond": {
        "total": "17.34",
        "ok": "17.29",
        "ko": "0.05"
    }
}
    }
}

}

function fillStats(stat){
    $("#numberOfRequests").append(stat.numberOfRequests.total);
    $("#numberOfRequestsOK").append(stat.numberOfRequests.ok);
    $("#numberOfRequestsKO").append(stat.numberOfRequests.ko);

    $("#minResponseTime").append(stat.minResponseTime.total);
    $("#minResponseTimeOK").append(stat.minResponseTime.ok);
    $("#minResponseTimeKO").append(stat.minResponseTime.ko);

    $("#maxResponseTime").append(stat.maxResponseTime.total);
    $("#maxResponseTimeOK").append(stat.maxResponseTime.ok);
    $("#maxResponseTimeKO").append(stat.maxResponseTime.ko);

    $("#meanResponseTime").append(stat.meanResponseTime.total);
    $("#meanResponseTimeOK").append(stat.meanResponseTime.ok);
    $("#meanResponseTimeKO").append(stat.meanResponseTime.ko);

    $("#standardDeviation").append(stat.standardDeviation.total);
    $("#standardDeviationOK").append(stat.standardDeviation.ok);
    $("#standardDeviationKO").append(stat.standardDeviation.ko);

    $("#percentiles1").append(stat.percentiles1.total);
    $("#percentiles1OK").append(stat.percentiles1.ok);
    $("#percentiles1KO").append(stat.percentiles1.ko);

    $("#percentiles2").append(stat.percentiles2.total);
    $("#percentiles2OK").append(stat.percentiles2.ok);
    $("#percentiles2KO").append(stat.percentiles2.ko);

    $("#percentiles3").append(stat.percentiles3.total);
    $("#percentiles3OK").append(stat.percentiles3.ok);
    $("#percentiles3KO").append(stat.percentiles3.ko);

    $("#percentiles4").append(stat.percentiles4.total);
    $("#percentiles4OK").append(stat.percentiles4.ok);
    $("#percentiles4KO").append(stat.percentiles4.ko);

    $("#meanNumberOfRequestsPerSecond").append(stat.meanNumberOfRequestsPerSecond.total);
    $("#meanNumberOfRequestsPerSecondOK").append(stat.meanNumberOfRequestsPerSecond.ok);
    $("#meanNumberOfRequestsPerSecondKO").append(stat.meanNumberOfRequestsPerSecond.ko);
}
