import java.util.*;

// ----- Neighbor class -----
class Neighbor {
    String neighbor;
    int distance;

    public Neighbor(String neighbor, int distance) {
        this.neighbor = neighbor;
        this.distance = distance;
    }

    public String getNeighborName() {
        return neighbor;
    }

    public int getDistance() {
        return distance;
    }
}

// ----- Station class -----
class Station {
    private String stationname;
    private String line;
    ArrayList<Neighbor> neighbours;

    public Station(String stationname, String line) {
        this.stationname = stationname;
        this.line = line;
        this.neighbours = new ArrayList<>();
    }

    public String getstationname() {
        return stationname;
    }

    public String getline() {
        return line;
    }

    public ArrayList<Neighbor> getNeighbors() {
        return neighbours;
    }
}

// ----- MetroMap class -----
class MetroMap {
    HashMap<String, Station> stations;

    public MetroMap() {
        stations = new HashMap<>();
    }

    public void addstation(String name, String line) {
        if (!stations.containsKey(name)) {
            stations.put(name, new Station(name, line));
        }
    }

    public int getnumberofstations() {
        return stations.size();
    }

    public void removestation(String name) {
        for (Station sta : stations.values()) {
            ArrayList<Neighbor> neighbors = sta.getNeighbors();
            for (int i = 0; i < neighbors.size(); i++) {
                if (neighbors.get(i).getNeighborName().equals(name)) {
                    neighbors.remove(i);
                    i--;
                }
            }
        }
        stations.remove(name);
    }

    public Station getStation(String name) {
        return stations.get(name);
    }

    public void addTrack(String src, String dest, int distance) {
        stations.get(src).getNeighbors().add(new Neighbor(dest, distance));
        stations.get(dest).getNeighbors().add(new Neighbor(src, distance));
    }

    public void removetrack(String src, String dest) {
        ArrayList<Neighbor> s = stations.get(src).getNeighbors();
        for (int i = 0; i < s.size(); i++) {
            if (s.get(i).getNeighborName().equals(dest)) {
                s.remove(i);
                i--;
            }
        }

        ArrayList<Neighbor> d = stations.get(dest).getNeighbors();
        for (int i = 0; i < d.size(); i++) {
            if (d.get(i).getNeighborName().equals(src)) {
                d.remove(i);
                i--;
            }
        }
    }

    public boolean hastrack(String src, String dest) {
        ArrayList<Neighbor> neighbors = stations.get(src).getNeighbors();
        for (Neighbor neighbor : neighbors) {
            if (neighbor.getNeighborName().equals(dest)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasstation(String stationname) {
        return stations.containsKey(stationname);
    }

    public Set<String> getAllStationNames() {
        return stations.keySet();
    }

    public ArrayList<Neighbor> getneighbors(String station) {
        return stations.get(station).getNeighbors();
    }

    public int getDistanceBetween(String src, String dest) {
        if (!stations.containsKey(src)) return -1;

        for (Neighbor neighbor : stations.get(src).getNeighbors()) {
            if (neighbor.getNeighborName().equals(dest)) {
                return neighbor.getDistance();
            }
        }

        return -1;
    }
}

// ----- RouteFinder class -----
class RouteFinder {
    private MetroMap metroMap;

    public RouteFinder(MetroMap metroMap) {
        this.metroMap = metroMap;
    }

    public class Pair {
        String stationname;
        String pathsofar;
        int cost;

        public Pair(String stationname, String pathsofar, int cost) {
            this.stationname = stationname;
            this.pathsofar = pathsofar;
            this.cost = cost;
        }
    }

    public class Result {
        String shortestpath;
        int cost;

        public Result(String shortestpath, int cost) {
            this.shortestpath = shortestpath;
            this.cost = cost;
        }
    }

    public Result dijkstra(String source, String destination) {
        PriorityQueue<Pair> pq = new PriorityQueue<>(Comparator.comparingInt(p -> p.cost));
        HashMap<String, Boolean> visited = new HashMap<>();
        HashMap<String, Integer> h = new HashMap<>();

        for (String st : metroMap.getAllStationNames()) {
            h.put(st, Integer.MAX_VALUE);
        }

        h.put(source, 0);
        pq.add(new Pair(source, source, 0));

        while (!pq.isEmpty()) {
            Pair node = pq.poll();

            if (visited.containsKey(node.stationname)) continue;
            visited.put(node.stationname, true);

            if (node.stationname.equals(destination)) {
                return new Result(node.pathsofar, node.cost);
            }

            int c = node.cost;

            for (Neighbor neigh : metroMap.getneighbors(node.stationname)) {
                String neighborName = neigh.getNeighborName();
                int weight = metroMap.getDistanceBetween(node.stationname, neighborName);

                if (!visited.containsKey(neighborName) && c + weight < h.get(neighborName)) {
                    h.put(neighborName, c + weight);
                    String path = node.pathsofar + " -> " + neighborName;
                    pq.add(new Pair(neighborName, path, c + weight));
                }
            }
        }

        return new Result("No path found", -1);
    }

    public Result bfs(String source, String destination) {
        Queue<Pair> queue = new LinkedList<>();
        HashSet<String> visited = new HashSet<>();

        queue.add(new Pair(source, source, 0));
        visited.add(source);

        while (!queue.isEmpty()) {
            Pair node = queue.poll();

            if (node.stationname.equals(destination)) {
                return new Result(node.pathsofar, node.cost);
            }

            for (Neighbor neighbor : metroMap.getneighbors(node.stationname)) {
                String neighName = neighbor.getNeighborName();
                if (!visited.contains(neighName)) {
                    visited.add(neighName);
                    String path = node.pathsofar + " -> " + neighName;
                    queue.add(new Pair(neighName, path, node.cost + 1));
                }
            }
        }

        return new Result("No path found", -1);
    }

    public void getInterchanges(String path) {
        String[] stations = path.split(" -> ");

        if (stations.length < 2) {
            System.out.println("No interchanges");
            return;
        }

        String prevStation = stations[0].trim();
        String prevLine = metroMap.getStation(prevStation).getline();

        for (int i = 1; i < stations.length - 1; i++) {
            String currentStation = stations[i].trim();
            String currentLine = metroMap.getStation(currentStation).getline();

            if (!prevLine.equals(currentLine)) {
                System.out.println("Interchange at " + currentStation + " (from " + prevLine + " to " + currentLine + ")");
            }

            prevStation = currentStation;
            prevLine = currentLine;
        }
    }
}

// ----- Main class -----
public class MetroApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MetroMap map = new MetroMap();

        // Add station and track setup here (same as in App.java)
        // [You can copy the App.java main content here as it is]

        System.out.println("\n\t**** WELCOME TO HYDERABAD METRO ROUTE FINDER ****");

        // Main menu code...
    }
}
