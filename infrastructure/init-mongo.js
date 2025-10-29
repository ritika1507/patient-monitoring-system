// Switch to vitals database
db = db.getSiblingDB("vitals_db");

// Create time-series collection for vitals
db.createCollection("vitals", {
  timeseries: {
    timeField: "timestamp",
    metaField: "patientId",
    granularity: "seconds",
  },
  expireAfterSeconds: 2592000,
});

// Create indexes for fast queries
db.vitals.createIndex({ patientId: 1, timestamp: -1 });
db.vitals.createIndex({ timestamp: -1 });

print("✅ Time-series collection created successfully");

// Insert sample data
db.vitals.insertMany([
  {
    patientId: "P001",
    heartRate: 75,
    bloodPressure: "120/80",
    oxygenLevel: 98,
    temperature: 36.8,
    timestamp: new Date(),
  },
  {
    patientId: "P002",
    heartRate: 82,
    bloodPressure: "118/75",
    oxygenLevel: 97,
    temperature: 37.1,
    timestamp: new Date(),
  },
]);

print("✅ Sample data inserted");
