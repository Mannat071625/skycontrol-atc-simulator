
import { useCallback, useEffect, useState } from "react";
import axios from "axios";
import {
  Plane, RadioTower, CalendarDays, AlertTriangle, CloudSun, History,
  LayoutDashboard, RefreshCw, Search, Plus, ArrowDown, MapPin,
  Wind, Eye, XCircle, Menu, X, Trash2
} from "lucide-react";
import "./App.css";

const api = axios.create({ baseURL: "/api" });

const navigation = [
  ["Dashboard", LayoutDashboard],
  ["Aircraft", Plane],
  ["Flights", CalendarDays],
  ["Runways & Gates", RadioTower],
  ["Emergencies", AlertTriangle],
  ["Weather", CloudSun],
  ["Flight History", History]
];

const emergencyTypes = [
  ["ENGINE_FAILURE", "Engine Failure"],
  ["LOW_FUEL", "Low Fuel"],
  ["MEDICAL", "Medical Emergency"],
  ["TECHNICAL", "Technical Issue"],
  ["OTHER", "Other"]
];

function Status({ value }) {
  const status = String(value || "UNKNOWN").toUpperCase();
  return <span className={`status status-${status.toLowerCase().replaceAll("_", "-")}`}>{status.replaceAll("_", " ")}</span>;
}

function Panel({ title, subtitle, children }) {
  return <section className="panel"><div className="panel-heading"><div><h2>{title}</h2>{subtitle && <p>{subtitle}</p>}</div></div>{children}</section>;
}

function Field({ label, children }) {
  return <label className="field"><span>{label}</span>{children}</label>;
}

function App() {
  const [page, setPage] = useState("Dashboard");
  const [aircraft, setAircraft] = useState([]);
  const [flights, setFlights] = useState([]);
  const [runways, setRunways] = useState([]);
  const [gates, setGates] = useState([]);
  const [weather, setWeather] = useState(null);
  const [restrictions, setRestrictions] = useState(null);
  const [emergencies, setEmergencies] = useState([]);
  const [history, setHistory] = useState([]);
  const [runwayQueue, setRunwayQueue] = useState([]);
  const [gateQueue, setGateQueue] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [noticeType, setNoticeType] = useState("success");
  const [search, setSearch] = useState("");
  const [mobileOpen, setMobileOpen] = useState(false);

  const [aircraftForm, setAircraftForm] = useState({
    aircraftId: "",
    flightNumber: "",
    airlineName: "",
    aircraftType: "PASSENGER",
    fuelLevel: 80,
    passengerCapacity: 180,
    cargoCapacity: 1,
    emergencyType: ""
  });

  const [flightForm, setFlightForm] = useState({
    flightNumber: "",
    source: "",
    destination: "",
    scheduledDeparture: "",
    scheduledArrival: "",
    aircraftId: ""
  });

  const [weatherForm, setWeatherForm] = useState({
    condition: "CLEAR",
    visibility: 10000,
    windSpeed: 5
  });

  const [showAircraftForm, setShowAircraftForm] = useState(false);
  const [showFlightForm, setShowFlightForm] = useState(false);

  const notify = (message, type = "success") => {
    setNotice(String(message));
    setNoticeType(type);
  };

  const loadData = useCallback(async () => {
    setLoading(true);
    setError("");

    const requests = [
      ["aircraft", "/aircraft", setAircraft],
      ["flights", "/flights", setFlights],
      ["runways", "/runways", setRunways],
      ["gates", "/gates", setGates],
      ["weather", "/weather", setWeather],
      ["restrictions", "/weather/restrictions", setRestrictions],
      ["emergencies", "/emergencies", setEmergencies],
      ["history", "/history", setHistory],
      ["runway queue", "/queues/runway", setRunwayQueue],
      ["gate queue", "/queues/gate", setGateQueue]
    ];

    const results = await Promise.allSettled(
      requests.map(item => api.get(item[1]))
    );

    const failed = [];

    results.forEach((result, index) => {
      if (result.status === "fulfilled") {
        requests[index][2](result.value.data);
      } else {
        failed.push(requests[index][0]);
      }
    });

    if (failed.length) {
      setError(`Could not load: ${failed.join(", ")}. Check the backend and refresh.`);
    }

    setLoading(false);
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(""), 4000);
    return () => clearTimeout(timer);
  }, [notice]);

  const runAction = async (action, successMessage) => {
    try {
      const response = await action();
      notify(successMessage || response.data?.message || "Operation completed.");
      await loadData();
      return true;
    } catch (err) {
      const message = err.response?.data?.message ||
        (typeof err.response?.data === "string" ? err.response.data : null) ||
        err.message ||
        "Operation failed.";

      notify(message, "error");
      return false;
    }
  };

  const registerAircraft = async e => {
    e.preventDefault();

    const data = {
      ...aircraftForm,
      fuelLevel: Number(aircraftForm.fuelLevel),
      passengerCapacity: Math.max(1, Number(aircraftForm.passengerCapacity)),
      cargoCapacity: Math.max(1, Number(aircraftForm.cargoCapacity))
    };

    const success = await runAction(
      () => api.post("/aircraft", data),
      "Aircraft registered successfully."
    );

    if (success) {
      setShowAircraftForm(false);
      setAircraftForm({
        aircraftId: "",
        flightNumber: "",
        airlineName: "",
        aircraftType: "PASSENGER",
        fuelLevel: 80,
        passengerCapacity: 180,
        cargoCapacity: 1,
        emergencyType: ""
      });
    }
  };

  const scheduleFlight = async e => {
    e.preventDefault();

    const success = await runAction(
      () => api.post("/flights", flightForm),
      "Flight scheduled successfully."
    );

    if (success) {
      setShowFlightForm(false);
      setFlightForm({
        flightNumber: "",
        source: "",
        destination: "",
        scheduledDeparture: "",
        scheduledArrival: "",
        aircraftId: ""
      });
    }
  };

  const updateWeather = e => {
    e.preventDefault();
    runAction(
      () => api.put("/weather", weatherForm),
      "Weather updated successfully."
    );
  };

  const processEmergency = () =>
    runAction(() => api.post("/emergencies/process"), "Emergency queue processing requested.");

  const processRunwayQueue = () =>
    runAction(() => api.post("/queues/runway/process"), "Runway queue processing requested.");

  const processGateQueue = () =>
    runAction(() => api.post("/queues/gate/process"), "Gate queue processing requested.");

  const filteredAircraft = aircraft.filter(a =>
    [a.aircraftId, a.flightNumber, a.airlineName, a.aircraftType, a.status]
      .join(" ").toLowerCase().includes(search.toLowerCase())
  );

  const filteredFlights = flights.filter(f =>
    [f.flightNumber, f.source, f.destination, f.status, f.aircraft?.aircraftId]
      .join(" ").toLowerCase().includes(search.toLowerCase())
  );

  const aircraftCounts = {
    total: aircraft.length,
    airborne: aircraft.filter(a => String(a.status).toUpperCase() === "AIRBORNE").length
  };

  const dashboard = (
    <>
      <div className="control-banner">
        <div>
          <span className="eyebrow">AIRPORT OPERATIONS · DEL</span>
          <h2>Airport Operations Board</h2>
          <p>Live operational data from your Air Traffic Control Simulator.</p>
        </div>
        <button className="refresh-btn" onClick={loadData}>
          <RefreshCw size={16}/> Refresh data
        </button>
      </div>

      <div className="operation-summary">
        <div><span>REGISTERED AIRCRAFT</span><strong>{aircraftCounts.total}</strong></div>
        <div><span>AIRBORNE</span><strong>{aircraftCounts.airborne}</strong></div>
        <div><span>AVAILABLE RUNWAYS</span><strong>{runways.filter(r => r.status === "AVAILABLE").length}/{runways.length}</strong></div>
        <div><span>EMERGENCY QUEUE</span><strong>{emergencies.length}</strong></div>
      </div>

      <div className="dashboard-layout">
        <Panel title="Airport surface operations" subtitle="Runway and gate status">
          <div className="surface-layout">
            <div className="surface-label">RUNWAYS</div>
            {runways.length ? runways.map(r => (
              <div className="surface-row" key={r.runwayId}>
                <div className="runway-graphic">
                  <span>{r.runwayId}</span>
                  <span className="runway-center">✦</span>
                  <span>{r.length} m</span>
                </div>
                <div className="surface-state">
                  <Status value={r.status}/>
                  <small>{r.assignedAircraft || "No aircraft assigned"}</small>
                </div>
              </div>
            )) : <p className="empty-state">No runway records returned.</p>}

            <div className="surface-label gate-label">GATES</div>
            <div className="gate-strip">
              {gates.length ? gates.map(g => (
                <div className="gate-strip-item" key={g.gateId}>
                  <strong>{g.gateId}</strong>
                  <Status value={g.status}/>
                  <small>{g.assignedAircraft || "Unassigned"}</small>
                </div>
              )) : <p className="empty-state">No gate records returned.</p>}
            </div>
          </div>
        </Panel>

        <Panel title="Current weather" subtitle="WeatherService data">
          {weather ? <>
            <div className="weather-reading">
              <CloudSun size={42}/>
              <div>
                <strong>{weather.condition}</strong>
                <span>{Number(weather.visibility).toLocaleString()} m visibility</span>
              </div>
            </div>
            <div className="weather-facts">
              <div><Wind size={17}/><span>Wind speed</span><strong>{weather.windSpeed} km/h</strong></div>
              <div><Eye size={17}/><span>Visibility</span><strong>{Number(weather.visibility).toLocaleString()} m</strong></div>
            </div>
            {restrictions && <div className="restriction-list">
              {Object.entries(restrictions).map(([key, value]) => (
                <div key={key}>
                  <span>{key.replaceAll(/([A-Z])/g, " $1")}</span>
                  <strong>{typeof value === "boolean" ? value ? "YES" : "NO" : String(value)}</strong>
                </div>
              ))}
            </div>}
          </> : <p className="empty-state">Weather data unavailable.</p>}
        </Panel>
      </div>

      <Panel title="Active flight strips" subtitle="Flights currently registered in the system">
        <FlightStrips flights={flights}/>
      </Panel>

      <div className="dashboard-bottom">
        <Panel title="Aircraft on record">
          <div className="mini-list">
            {aircraft.length ? aircraft.map(a => (
              <div key={a.aircraftId}>
                <Plane size={17}/>
                <div>
                  <strong>{a.flightNumber}</strong>
                  <small>{a.aircraftId} · {a.airlineName}</small>
                </div>
                <Status value={a.status}/>
              </div>
            )) : <p className="empty-state">No registered aircraft.</p>}
          </div>
        </Panel>

        <Panel title="Emergency queue">
          <div className="mini-list">
            {emergencies.length ? emergencies.map((e, i) => (
              <div key={e.aircraftId || i}>
                <AlertTriangle size={17}/>
                <div>
                  <strong>{e.aircraftId || "Aircraft"}</strong>
                  <small>{e.emergencyType || e.details || "Emergency reported"}</small>
                </div>
                <Status value="EMERGENCY"/>
              </div>
            )) : <p className="empty-state">No active emergencies.</p>}
          </div>
        </Panel>
      </div>
    </>
  );

  const aircraftPage = (
    <>
      <div className="page-actions">
        <div className="search-box">
          <Search size={17}/>
          <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search aircraft..."/>
        </div>
        <button className="primary-btn" onClick={() => setShowAircraftForm(!showAircraftForm)}>
          <Plus size={16}/> Register aircraft
        </button>
      </div>

      {showAircraftForm && (
        <Panel title="Register aircraft">
          <form className="form-grid" onSubmit={registerAircraft}>
            <Field label="Aircraft ID">
              <input required value={aircraftForm.aircraftId} onChange={e => setAircraftForm({...aircraftForm, aircraftId: e.target.value})}/>
            </Field>
            <Field label="Flight number">
              <input required value={aircraftForm.flightNumber} onChange={e => setAircraftForm({...aircraftForm, flightNumber: e.target.value})}/>
            </Field>
            <Field label="Airline">
              <input required value={aircraftForm.airlineName} onChange={e => setAircraftForm({...aircraftForm, airlineName: e.target.value})}/>
            </Field>
            <Field label="Aircraft type">
              <select value={aircraftForm.aircraftType} onChange={e => setAircraftForm({...aircraftForm, aircraftType: e.target.value})}>
                <option value="PASSENGER">Passenger</option>
                <option value="CARGO">Cargo</option>
                <option value="EMERGENCY">Emergency</option>
              </select>
            </Field>
            <Field label="Fuel level (%)">
              <input type="number" min="0" max="100" required value={aircraftForm.fuelLevel} onChange={e => setAircraftForm({...aircraftForm, fuelLevel: Number(e.target.value)})}/>
            </Field>

            {aircraftForm.aircraftType === "PASSENGER" && (
              <Field label="Passenger capacity">
                <input type="number" min="1" required value={aircraftForm.passengerCapacity} onChange={e => setAircraftForm({...aircraftForm, passengerCapacity: Number(e.target.value)})}/>
              </Field>
            )}

            {aircraftForm.aircraftType === "CARGO" && (
              <Field label="Cargo capacity">
                <input type="number" min="1" required value={aircraftForm.cargoCapacity} onChange={e => setAircraftForm({...aircraftForm, cargoCapacity: Number(e.target.value)})}/>
              </Field>
            )}

            {aircraftForm.aircraftType === "EMERGENCY" && (
              <Field label="Emergency type">
                <select required value={aircraftForm.emergencyType} onChange={e => setAircraftForm({...aircraftForm, emergencyType: e.target.value})}>
                  <option value="">Select emergency type</option>
                  {emergencyTypes.map(([value, label]) => (
                    <option key={value} value={value}>{label}</option>
                  ))}
                </select>
              </Field>
            )}

            <button className="primary-btn" type="submit">Save aircraft</button>
          </form>
        </Panel>
      )}

      <Panel title="Aircraft registry" subtitle={`${filteredAircraft.length} matching records`}>
        <div className="table-scroll">
          <table>
            <thead>
              <tr><th>AIRCRAFT</th><th>FLIGHT</th><th>AIRLINE</th><th>TYPE</th><th>FUEL</th><th>STATUS</th><th>ACTION</th></tr>
            </thead>
            <tbody>
              {filteredAircraft.map(a => (
                <tr key={a.aircraftId}>
                  <td><strong>{a.aircraftId}</strong></td>
                  <td>{a.flightNumber}</td>
                  <td>{a.airlineName}</td>
                  <td>{a.aircraftType}</td>
                  <td>{a.fuelLevel}%</td>
                  <td><Status value={a.status}/></td>
                  <td>
                    <button className="icon-action danger" title="Delete aircraft" onClick={() => {
                      if (window.confirm(`Delete ${a.aircraftId}?`)) {
                        runAction(() => api.delete(`/aircraft/${a.aircraftId}`), "Aircraft deleted.");
                      }
                    }}>
                      <Trash2 size={16}/>
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!filteredAircraft.length && <p className="empty-state">No aircraft records found.</p>}
        </div>
      </Panel>
    </>
  );

  const flightsPage = (
    <>
      <div className="page-actions">
        <div className="search-box">
          <Search size={17}/>
          <input value={search} onChange={e => setSearch(e.target.value)} placeholder="Search flights..."/>
        </div>
        <button className="primary-btn" onClick={() => setShowFlightForm(!showFlightForm)}>
          <Plus size={16}/> Schedule flight
        </button>
      </div>

      {showFlightForm && (
        <Panel title="Schedule a flight">
          <form className="form-grid" onSubmit={scheduleFlight}>
            <Field label="Flight number">
              <input required value={flightForm.flightNumber} onChange={e => setFlightForm({...flightForm, flightNumber: e.target.value})}/>
            </Field>
            <Field label="Aircraft">
              <select required value={flightForm.aircraftId} onChange={e => setFlightForm({...flightForm, aircraftId: e.target.value})}>
                <option value="">Select aircraft</option>
                {aircraft.map(a => <option key={a.aircraftId} value={a.aircraftId}>{a.aircraftId} · {a.flightNumber}</option>)}
              </select>
            </Field>
            <Field label="Source">
              <input required value={flightForm.source} onChange={e => setFlightForm({...flightForm, source: e.target.value})}/>
            </Field>
            <Field label="Destination">
              <input required value={flightForm.destination} onChange={e => setFlightForm({...flightForm, destination: e.target.value})}/>
            </Field>
            <Field label="Scheduled departure">
              <input type="datetime-local" required value={flightForm.scheduledDeparture} onChange={e => setFlightForm({...flightForm, scheduledDeparture: e.target.value})}/>
            </Field>
            <Field label="Scheduled arrival">
              <input type="datetime-local" required value={flightForm.scheduledArrival} onChange={e => setFlightForm({...flightForm, scheduledArrival: e.target.value})}/>
            </Field>
            <button className="primary-btn" type="submit">Create flight</button>
          </form>
        </Panel>
      )}

      <Panel title="Flight timetable" subtitle={`${filteredFlights.length} flight records`}>
        <div className="table-scroll">
          <table>
            <thead>
              <tr><th>FLIGHT</th><th>AIRCRAFT</th><th>ROUTE</th><th>DEPARTURE</th><th>ARRIVAL</th><th>STATUS</th><th>UPDATE</th></tr>
            </thead>
            <tbody>
              {filteredFlights.map(f => (
                <tr key={f.flightNumber}>
                  <td><strong>{f.flightNumber}</strong></td>
                  <td>{f.aircraft?.aircraftId || "—"}</td>
                  <td>{f.source} → {f.destination}</td>
                  <td>{String(f.scheduledDeparture).replace("T", " ")}</td>
                  <td>{String(f.scheduledArrival).replace("T", " ")}</td>
                  <td><Status value={f.status}/></td>
                  <td>
                    <select className="inline-select" value={f.status} onChange={e => runAction(
                      () => api.put(`/flights/${f.flightNumber}/status`, null, {params: {status: e.target.value}}),
                      "Flight status updated."
                    )}>
                      <option value={f.status}>{f.status}</option>
                      {["SCHEDULED", "BOARDING", "DELAYED", "CANCELLED", "COMPLETED"].filter(s => s !== f.status).map(s => <option key={s}>{s}</option>)}
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {!filteredFlights.length && <p className="empty-state">No scheduled flights.</p>}
        </div>
      </Panel>

      <Panel title="Flight strips">
        <FlightStrips flights={flights}/>
      </Panel>
    </>
  );

  const runwaysPage = (
    <>
      <Panel title="Runway control" subtitle="Live runway records from the backend">
        <div className="resource-list">
          {runways.map(r => (
            <div className="resource-row" key={r.runwayId}>
              <div className="runway-graphic"><span>{r.runwayId}</span><span>{r.length} m</span></div>
              <div className="resource-meta"><Status value={r.status}/><small>{r.assignedAircraft || "No aircraft assigned"}</small></div>
              <div className="resource-actions">
                <select id={`aircraft-${r.runwayId}`}>
                  <option value="">Select aircraft</option>
                  {aircraft.map(a => <option key={a.aircraftId} value={a.aircraftId}>{a.aircraftId}</option>)}
                </select>
                <button disabled={r.status === "OCCUPIED"} onClick={() => {
                  const id = document.getElementById(`aircraft-${r.runwayId}`).value;
                  if (id) runAction(() => api.put(`/runways/${r.runwayId}/occupy`, null, {params: {aircraftId: id}}), "Runway occupied.");
                  else notify("Select an aircraft first.", "error");
                }}>Occupy</button>
                <button disabled={r.status !== "OCCUPIED"} onClick={() => runAction(() => api.put(`/runways/${r.runwayId}/release`), "Runway released.")}>Release</button>
              </div>
            </div>
          ))}
          {!runways.length && <p className="empty-state">No runway records.</p>}
        </div>
      </Panel>

      <Panel title="Gate allocation" subtitle="Current gate records">
        <div className="resource-list">
          {gates.map(g => (
            <div className="resource-row" key={g.gateId}>
              <div className="gate-name"><MapPin size={19}/><strong>{g.gateId}</strong></div>
              <div className="resource-meta"><Status value={g.status}/><small>{g.assignedAircraft || "No aircraft assigned"}</small></div>
              <div className="resource-actions">
                <select id={`gate-${g.gateId}`}>
                  <option value="">Select aircraft</option>
                  {aircraft.map(a => <option key={a.aircraftId} value={a.aircraftId}>{a.aircraftId}</option>)}
                </select>
                <button disabled={g.status === "OCCUPIED"} onClick={() => {
                  const id = document.getElementById(`gate-${g.gateId}`).value;
                  if (id) runAction(() => api.put(`/gates/${g.gateId}/occupy`, null, {params: {aircraftId: id}}), "Gate occupied.");
                  else notify("Select an aircraft first.", "error");
                }}>Occupy</button>
                <button disabled={g.status !== "OCCUPIED"} onClick={() => runAction(() => api.put(`/gates/${g.gateId}/release`), "Gate released.")}>Release</button>
              </div>
            </div>
          ))}
          {!gates.length && <p className="empty-state">No gate records.</p>}
        </div>
      </Panel>

      <div className="queue-grid">
        <Panel title={`Runway queue · ${runwayQueue.length}`}>
          <QueueList items={runwayQueue}/>
          <button className="primary-btn" onClick={processRunwayQueue}>Process runway queue</button>
        </Panel>
        <Panel title={`Gate queue · ${gateQueue.length}`}>
          <QueueList items={gateQueue}/>
          <button className="primary-btn" onClick={processGateQueue}>Process gate queue</button>
        </Panel>
      </div>
    </>
  );

  const emergenciesPage = (
    <>
      <Panel title="Emergency response" subtitle="Emergency records returned by EmergencyHandler">
        <div className="emergency-heading">
          <AlertTriangle size={23}/>
          <strong>{emergencies.length} active emergency record(s)</strong>
        </div>
        {emergencies.length ? (
          <div className="table-scroll">
            <table>
              <thead><tr><th>AIRCRAFT</th><th>EMERGENCY DETAILS</th><th>STATUS</th></tr></thead>
              <tbody>{emergencies.map((e, i) => (
                <tr key={e.aircraftId || i}>
                  <td>{e.aircraftId}</td>
                  <td>{e.emergencyType || e.details || "—"}</td>
                  <td><Status value="EMERGENCY"/></td>
                </tr>
              ))}</tbody>
            </table>
          </div>
        ) : <p className="empty-state">No active emergencies. The emergency queue is empty.</p>}
        <button className="primary-btn" onClick={processEmergency}>Process emergency queue</button>
      </Panel>

      <Panel title="Report an emergency">
        <form className="form-grid" onSubmit={e => {
          e.preventDefault();
          const id = e.target.aircraftId.value;
          runAction(() => api.post("/emergencies", {aircraftId: id}), "Emergency reported.");
          e.target.reset();
        }}>
          <Field label="Aircraft">
            <select name="aircraftId" required>
              <option value="">Select aircraft</option>
              {aircraft.map(a => <option key={a.aircraftId} value={a.aircraftId}>{a.aircraftId} · {a.flightNumber}</option>)}
            </select>
          </Field>
          <button className="primary-btn" type="submit">Report emergency</button>
        </form>
      </Panel>
    </>
  );

  const weatherPage = (
    <>
      <Panel title="Current weather conditions" subtitle="Live values from WeatherService">
        {weather ? <>
          <div className="weather-reading">
            <CloudSun size={45}/>
            <div><strong>{weather.condition}</strong><span>Current airport conditions</span></div>
          </div>
          <div className="weather-facts">
            <div><Wind/><span>Wind speed</span><strong>{weather.windSpeed} km/h</strong></div>
            <div><Eye/><span>Visibility</span><strong>{Number(weather.visibility).toLocaleString()} m</strong></div>
          </div>
        </> : <p className="empty-state">Weather data unavailable.</p>}
        {restrictions && <div className="restriction-list">
          {Object.entries(restrictions).map(([key, value]) => (
            <div key={key}>
              <span>{key.replaceAll(/([A-Z])/g, " $1")}</span>
              <strong>{typeof value === "boolean" ? value ? "RESTRICTED" : "ALLOWED" : String(value)}</strong>
            </div>
          ))}
        </div>}
      </Panel>

      <Panel title="Update weather">
        <form className="form-grid" onSubmit={updateWeather}>
          <Field label="Condition">
            <select value={weatherForm.condition} onChange={e => setWeatherForm({...weatherForm, condition: e.target.value})}>
              {["CLEAR", "CLOUDY", "FOG", "RAIN", "STORM", "THUNDERSTORM"].map(c => <option key={c}>{c}</option>)}
            </select>
          </Field>
          <Field label="Visibility (metres)">
            <input type="number" min="0" required value={weatherForm.visibility} onChange={e => setWeatherForm({...weatherForm, visibility: Number(e.target.value)})}/>
          </Field>
          <Field label="Wind speed (km/h)">
            <input type="number" min="0" required value={weatherForm.windSpeed} onChange={e => setWeatherForm({...weatherForm, windSpeed: Number(e.target.value)})}/>
          </Field>
          <button className="primary-btn" type="submit">Update conditions</button>
        </form>
      </Panel>
    </>
  );

  const historyPage = (
    <Panel title="Flight history" subtitle={`${history.length} recorded operational events`}>
      <div className="table-scroll">
        <table>
          <thead><tr><th>TIME</th><th>FLIGHT</th><th>AIRCRAFT</th><th>EVENT</th><th>DETAILS</th></tr></thead>
          <tbody>
            {history.map((h, i) => (
              <tr key={h.id || i}>
                <td>{h.timestamp ? String(h.timestamp).replace("T", " ") : "—"}</td>
                <td><strong>{h.flightNumber}</strong></td>
                <td>{h.aircraftId}</td>
                <td><Status value={h.event}/></td>
                <td>{h.details}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {!history.length && <p className="empty-state">No flight history records.</p>}
      </div>
    </Panel>
  );

  const pages = {
    Dashboard: dashboard,
    Aircraft: aircraftPage,
    Flights: flightsPage,
    "Runways & Gates": runwaysPage,
    Emergencies: emergenciesPage,
    Weather: weatherPage,
    "Flight History": historyPage
  };

  const title = page === "Flights" ? "Flight Schedule" : page;

  return (
    <div className="atc-shell">
      <aside className={`sidebar ${mobileOpen ? "sidebar-open" : ""}`}>
        <div className="brand">
          <RadioTower size={25}/>
          <div><strong>SKYCONTROL</strong><small>ATC OPERATIONS</small></div>
          <button className="close-menu" onClick={() => setMobileOpen(false)}><X/></button>
        </div>
        <div className="nav-caption">OPERATIONS</div>
        <nav>
          {navigation.map(([name, Icon]) => (
            <button key={name} className={`nav-item ${page === name ? "active" : ""}`} onClick={() => {
              setPage(name);
              setSearch("");
              setMobileOpen(false);
            }}>
              <Icon size={18}/>{name}
            </button>
          ))}
        </nav>
        <div className="sidebar-footer">
          <span className="online-indicator"/> BACKEND CONNECTION
          <strong>{loading ? "SYNCING" : error ? "CHECK REQUIRED" : "CONNECTED"}</strong>
        </div>
      </aside>

      {mobileOpen && <div className="mobile-overlay" onClick={() => setMobileOpen(false)}/>}

      <main className="main-area">
        <header className="topbar">
          <button className="menu-toggle" onClick={() => setMobileOpen(true)}><Menu/></button>
          <div className="topbar-title">DEL <span>/</span> {title.toUpperCase()}</div>
          <div className="topbar-right">
            <span className="connection"><i/> {error ? "API ISSUE" : "LIVE API"}</span>
            <button className="refresh-btn" onClick={loadData}><RefreshCw size={15}/></button>
          </div>
        </header>

        <div className="content">
          <div className="page-heading">
            <div><span className="eyebrow">AIR TRAFFIC CONTROL · DEL</span><h1>{title}</h1></div>
            {(page === "Aircraft" || page === "Flights") && (
              <span className="record-count">{page === "Aircraft" ? aircraft.length : flights.length} RECORDS</span>
            )}
          </div>

          {error && <div className="error-banner"><XCircle size={17}/>{error}</div>}
          {notice && <div className={noticeType === "error" ? "error-banner" : "notice-banner"}>{notice}</div>}

          {loading ? (
            <div className="loading-state"><RefreshCw className="spin"/> Loading backend data...</div>
          ) : pages[page]}

          <footer>
            SKYCONTROL · AIR TRAFFIC CONTROL SIMULATOR
            <span>DATA SOURCE: SPRING BOOT API + MYSQL</span>
          </footer>
        </div>
      </main>
    </div>
  );
}

function FlightStrips({ flights }) {
  return flights.length ? (
    <div className="flight-strips">
      {flights.map(f => (
        <div className="flight-strip" key={f.flightNumber}>
          <div className="strip-call">
            <strong>{f.flightNumber}</strong>
            <span>{f.aircraft?.aircraftId || "Aircraft unavailable"}</span>
          </div>
          <div className="strip-route">
            <span>{f.source}</span><ArrowDown size={14}/><span>{f.destination}</span>
          </div>
          <div className="strip-times">
            <span>DEP {String(f.scheduledDeparture).replace("T", " ")}</span>
            <span>ARR {String(f.scheduledArrival).replace("T", " ")}</span>
          </div>
          <Status value={f.status}/>
        </div>
      ))}
    </div>
  ) : <p className="empty-state">No flights registered in the system.</p>;
}

function QueueList({ items }) {
  return items.length ? (
    <div className="queue-list">
      {items.map((item, i) => (
        <div key={item.aircraftId || i}>
          <strong>{item.aircraftId || String(item)}</strong>
          <span>{item.flightNumber || item.priority || ""}</span>
        </div>
      ))}
    </div>
  ) : <p className="empty-state">Queue is empty.</p>;
}

export default App;
