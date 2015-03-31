import java.sql.*;
import java.util.*;

/**
 * This Class is Package for Search Directions Place A to Place B
 * Using Undirected Graph by Map,Hashmap,List, getting data from database
 * The Original Code is http://codereview.stackexchange.com/questions/38376/a-search-algorithm
 * Created by jitsuem on 3/6/2015.
 */
final class NodeData<T> {

    private final T nodeId;

    private double g;  // g is distance from the source
    private double h;  // h is the heuristic of destination.
    private double f;  // f = g + h

    public NodeData (T nodeId, String heuristic) {
        this.nodeId = nodeId;
        this.g = Double.MAX_VALUE;
    }

    public T getNodeId() {
        return nodeId;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getH() {
        return h;
    }

    public double getF() {
        return f;
    }
}

/**
 * The graph represents an undirected graph.
 *
 * @author SERVICE-NOW\ameya.patil
 *
 * @param <T>
 */
final class GraphAStar<T> implements Iterable<T> {
    /*
     * A map from the nodeId to outgoing edge.
     * An outgoing edge is represented as a tuple of NodeData and the edge length
     */
    private final Map<T, Map<NodeData<T>, Double>> graph;
    /*
     * A map of heuristic from a node to each other node in the graph.
     */
    private final Map<String, String> heuristicMap;
    /*
     * A map between nodeId and nodedata.
     */
    private final Map<T, NodeData<T>> nodeIdNodeData;

    public GraphAStar(Map<String, String> heuristicMap) {
        if (heuristicMap == null) throw new NullPointerException("The heuristic map should not be null");
        graph = new HashMap<>();
        nodeIdNodeData = new HashMap<>();
        this.heuristicMap = heuristicMap;
    }

    /**
     * Adds a new node to the graph.
     * Internally it creates the nodeData and populates the heuristic map concerning input node into node data.
     *
     * @param nodeId the node to be added
     */
    public void addNode(T nodeId) {
        if (nodeId == null) throw new NullPointerException("The node cannot be null");
        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of heuristic map");

        graph.put(nodeId, new HashMap<NodeData<T>, Double>());
        nodeIdNodeData.put(nodeId, new NodeData<>(nodeId, heuristicMap.get(nodeId)));
    }

    /**
     * Adds an edge from source node to destination node.
     * There can only be a single edge from source to node.
     * Adding additional edge would overwrite the value
     *
     * @param nodeIdFirst   the first node to be in the edge
     * @param nodeIdSecond  the second node to be second node in the edge
     * @param length        the length of the edge.
     */
    public void addEdge(T nodeIdFirst, T nodeIdSecond, double length) {
        if (nodeIdFirst == null || nodeIdSecond == null) throw new NullPointerException("The first nor second node can be null.");

        if (!heuristicMap.containsKey(nodeIdFirst) || !heuristicMap.containsKey(nodeIdSecond)) {
            throw new NoSuchElementException("Source and Destination both should be part of the part of heuristic map");
        }
        if (!graph.containsKey(nodeIdFirst) || !graph.containsKey(nodeIdSecond)) {
            throw new NoSuchElementException("Source and Destination both should be part of the part of graph");
        }

        graph.get(nodeIdFirst).put(nodeIdNodeData.get(nodeIdSecond), length);
        graph.get(nodeIdSecond).put(nodeIdNodeData.get(nodeIdFirst), length);
    }

    /**
     * Returns immutable view of the edges
     *
     * @param nodeId    the nodeId whose outgoing edge needs to be returned
     * @return          An immutable view of edges leaving that node
     */
    public Map<NodeData<T>, Double> edgesFrom (T nodeId) {
        if (nodeId == null) throw new NullPointerException("The input node should not be null.");
        if (!heuristicMap.containsKey(nodeId)) throw new NoSuchElementException("This node is not a part of heuristic map");
        if (!graph.containsKey(nodeId)) throw new NoSuchElementException("The node should not be null.");

        return Collections.unmodifiableMap(graph.get(nodeId));
    }

    /**
     * The nodedata corresponding to the current nodeId.
     *
     * @param nodeId    the nodeId to be returned
     * @return          the nodeData from the
     */
    public NodeData<T> getNodeData (T nodeId) {
        if (nodeId == null) { throw new NullPointerException("The nodeid should not be empty"); }
        if (!nodeIdNodeData.containsKey(nodeId))  { throw new NoSuchElementException("The nodeId does not exist"); }
        return nodeIdNodeData.get(nodeId);
    }

    /**
     * Returns an iterator that can traverse the nodes of the graph
     *
     * @return an Iterator.
     */
    @Override public Iterator<T> iterator() {
        return graph.keySet().iterator();
    }
}

public class astart<T> {
    private Double distance;
    private String Source,Destinaion;

    private final GraphAStar<T> graph;

    private astart(GraphAStar<T> graphAStar) {
        this.graph = graphAStar;
    }

    // extend comparator.
    private class NodeComparator implements Comparator<NodeData<T>> {
        public int compare(NodeData<T> nodeFirst, NodeData<T> nodeSecond) {
            if (nodeFirst.getF() > nodeSecond.getF()) return 1;
            if (nodeSecond.getF() > nodeFirst.getF()) return -1;
            return 0;
        }
    }

    /**
     * Implements the A-star algorithm and returns the path from source to destination
     *
     * @param source        the source nodeid
     * @param destination   the destination nodeid
     * @return              the path from source to destination
     */
    List<T> astar(T source, T destination) {
        /**
         * http://stackoverflow.com/questions/20344041/why-does-priority-queue-has-default-initial-capacity-of-11
         */
        Source = (String)source;
        Destinaion = (String)destination;
        final Queue<NodeData<T>> openQueue = new PriorityQueue<>(11, new NodeComparator());

        NodeData<T> sourceNodeData = graph.getNodeData(source);
        sourceNodeData.setG(0);
        //sourceNodeData.calcF(destination);
        openQueue.add(sourceNodeData);

        final Map<T, T> path = new HashMap<>();
        final Set<NodeData<T>> closedList = new HashSet<>();

        while (!openQueue.isEmpty()) {
            final NodeData<T> nodeData = openQueue.poll();

            if (nodeData.getNodeId().equals(destination)) {
                return path(path, destination);
            }

            closedList.add(nodeData);

            for (Map.Entry<NodeData<T>, Double> neighborEntry : graph.edgesFrom(nodeData.getNodeId()).entrySet()) {
                NodeData<T> neighbor = neighborEntry.getKey();

                if (closedList.contains(neighbor)) continue;

                double distanceBetweenTwoNodes = neighborEntry.getValue();
                double tentativeG = distanceBetweenTwoNodes + nodeData.getG();


                if (tentativeG < neighbor.getG()) {
                    neighbor.setG(tentativeG);
                    distance = tentativeG;
                    //neighbor.calcF(destination);

                    path.put(neighbor.getNodeId(), nodeData.getNodeId());
                    if (!openQueue.contains(neighbor)) {
                        openQueue.add(neighbor);
                    }
                }
            }
        }

        return null;
    }



    private List<T> path(Map<T, T> path, T destination) {
        assert path != null;
        assert destination != null;

        final List<T> pathList = new ArrayList<>();
        pathList.add(destination);
        while (path.containsKey(destination)) {
            destination = path.get(destination);
            pathList.add(destination);
        }
        Collections.reverse(pathList);
        return pathList;
    }


    public static void main(String[] args) throws SQLException {
        Map<String, String> heuristic = new HashMap<>();
        /**
         * Connecting To Database*/
        String host = "jdbc:mysql://gameparty.zapto.org:3306/";
        String user = "pongpat";
        String pass = "12031991";
        Connection connection = DriverManager.getConnection(host,user,pass);
        Statement statement = connection.createStatement() ;
        String pathQuery = "SELECT * FROM newschema.paths";
        String stationsQuery = "SELECT * FROM newschema.stations";
        ResultSet stations = statement.executeQuery(stationsQuery);

        /**
         * Querying Stations Data into heuristic Map*/

        int station_type = stations.findColumn("station_type");
        int station_name = stations.findColumn("station_name");
        String station_name_str;
        List name = new ArrayList();
        while (stations.next()){
            station_name_str = stations.getString(station_name);
            name.add(station_name_str);
            heuristic.put(station_name_str,stations.getString(station_type));
        }
        /**Bring the heuristic into The Graph and create node into the Graph*/
        GraphAStar<String> graph = new GraphAStar<>(heuristic);
        for (Object str:name){
            graph.addNode(String.valueOf(str));
        }

        /**Querying path data into the Graph and add Edge into the Graph then Closing the Connection*/
        Statement path_statement = connection.createStatement() ;
        ResultSet paths = path_statement.executeQuery(pathQuery);

        int path_station_A = paths.findColumn("station_a");
        int path_station_B = paths.findColumn("station_b");
        int path_station_distance = paths.findColumn("distance");
        while (paths.next()){
            graph.addEdge(paths.getString(path_station_A),paths.getString(path_station_B),paths.getDouble(path_station_distance));
        }
        connection.close();

        /*Querying Direction by end-user */
        String a="ARL สุวรรณภูมิ";
        String b="BRT วัดดอกไม้";

        astart<String> aStar = new astart<>(graph);
        for (String path : aStar.astar(a, b)) {
            System.out.print(" \n " + path);
        }
        System.out.println("\n Distance "+aStar.Source+" to "+aStar.Destinaion+"  = "+aStar.distance);




       /* graph.edgesFrom("A");//edge data at node
        graph.getNodeData("A").getG();//get distance to source
        graph.getNodeData("A").getH();//get distance to destination
        graph.getNodeData("A").getF();//get distance source to destinations

        mapA.get("B");//how to get data B in Map A
*/
        Runtime runtime = Runtime.getRuntime();
        System.out.println("allocated memory: " + runtime.totalMemory() / 1024);
    }
}
