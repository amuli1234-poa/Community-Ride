var map = L.map('map').setView([0, 0], 2);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(map);

var blueDotIcon = L.divIcon({
    className: "custom-marker-icon blue-dot",
    iconSize: [16, 16],
    iconAnchor: [8, 8],
    popupAnchor: [0, -10]
});

var currentLocationMarker = null;

function onLocationFound(e) {
    var radius = e.accuracy / 2;
    var latlng = e.latlng;

    if (currentLocationMarker) {
        map.removeLayer(currentLocationMarker);
    }

    currentLocationMarker = L.marker(latlng, {icon: blueDotIcon}).addTo(map)
        .bindPopup("You are within " + radius + " meters from this point").openPopup();

    L.circle(latlng, radius).addTo(map);
    map.setView(latlng, 13);
}

function onLocationError(e) {
    alert(e.message);
}

map.on('locationfound', onLocationFound);
map.on('locationerror', onLocationError);

map.locate({setView: true, maxZoom: 16, watch: true, enableHighAccuracy: true});

// Function to add markers dynamically (e.g., from Android code)
function addMarker(lat, lng, title, snippet) {
    var newMarker = L.marker([lat, lng]).addTo(map);
    if (title || snippet) {
        newMarker.bindPopup("<b>" + (title || "") + "</b><br>" + (snippet || "")).openPopup();
    }
}
