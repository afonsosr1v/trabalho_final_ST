/*
 * Sistemas de Telecomunicacoes 
 *          2022/2023
 */
package protocol;

import simulator.AckFrameIF;
import simulator.DataFrameIF;
import terminal.Simulator;
import simulator.Frame;
import terminal.NetworkLayer;
import terminal.Terminal;  

/**
 * Protocol 4 : Go-back-N protocol
 *
 * @author 62429 (Put here your students' numbers)
 */
public class GoBackN extends Base_Protocol implements Callbacks {

    public GoBackN(Simulator _sim, NetworkLayer _net) {
        super(_sim, _net);      // Calls the constructor of Base_Protocol

        // Initialize here all variables
        // ...
    }
    
    private void send_next_data_packet(){
        if(sending_buffer!=null){
            Frame frame = Frame.new_Data_Frame(next_frame_to_send, prev_seq(0), net.get_recvbuffsize(), sending_buffer);
            sim.to_physical_layer(frame, false);
        }
    }

    /**
     * CALLBACK FUNCTION: handle the beginning of the simulation event
     *
     * @param time current simulation time
     */
    @Override
    public void start_simulation(long time) {
        sim.Log("\nGo-Back-N Protocol\n\n");
        sending_buffer = net.from_network_layer();
        send_next_data_packet();
    }

    /**
     * CALLBACK FUNCTION: handle the end of Data frame transmission, start timer
     * and send next until reaching the end of the sending window.
     *
     * @param time current simulation time
     * @param seq sequence number of the Data frame transmitted
     */
    @Override
    public void handle_Data_end(long time, int seq) {
        if(nak_failed_to_send) {
            nak_failed_to_send = false;
            send_nak();
        }
        else {
            if(frames_sent_by < sim.get_max_sequence()&& frames_sent < sim.get_send_window()){
                if(!data_failed_to_send_by &&(net.has_more_packets_to_send() || ta_a_mandar)){
                    
                }
            }
        }
    }

    /**
     * CALLBACK FUNCTION: handle the timer event; retransmit failed frames.
     *
     * @param time current simulation time
     * @param key  timer key (sequence number)
     */
    @Override
    public void handle_Data_Timer(long time, int key) {
        send_next_data_packet();
    }

    /**
     * CALLBACK FUNCTION: handle the ack timer event; send ACK frame
     *
     * @param time current simulation time
     */
    @Override
    public void handle_ack_Timer(long time) {
       sim.Log(time+" ACK Timout - ignored\n");
    }

    /**
     * CALLBACK FUNCTION: handle the reception of a frame from the physical
     * layer
     *
     * @param time current simulation time
     * @param frame frame received
     */
    @Override
    public void from_physical_layer(long time, Frame frame) {
       // sim.Log("from_physical_layer not implemented\n");
       if(frame.kind()==Frame.ACK_FRAME) { //snd
           if(frame.ack()==next_frame_to_send){
               sim.cancel_data_timer(next_frame_to_send);
               
               sim.Log("Recebeu"+time+next_frame_to_send+"\n");
               
               next_frame_to_send = next_seq(next_frame_to_send);
               sending_buffer = net.from_network_layer();
               send_next_data_packet();
           }
       }
       else { //rcv
           DataFrameIF dframe=frame;
           if(dframe.seq()==frame_expected){
               if (net.to_network_layer(dframe.info())){
                   frame_expected = next_seq(frame_expected);
               }
           }
       }
       Frame ack = Frame.new_Ack_Frame(frame.seq(), net.get_recvbuffsize());
       sim.to_physical_layer(ack, false);
    }

    /**
     * CALLBACK FUNCTION: handle the end of the simulation
     *
     * @param time current simulation time
     */
    @Override
    public void end_simulation(long time) {
        sim.Log("Stopping simulation\n");
    }

    /* Variables */
    /**
     * Reference to the simulator (Terminal), to get the configuration and send
     * commands
     */
    //final Simulator sim;  -  Inherited from Base_Protocol
    /**
     * Reference to the network layer, to send a receive packets
     */
    //final NetworkLayer net;    -  Inherited from Base_Protocol
    
    private int next_frame_to_send;
    private String sending_buffer;
    private int frame_expected;
    private boolean nak_failed_to_send;
    private int frames_sent_by;
    private boolean data_failed_to_send_by;
    private int frames_sent;
    private boolean ta_a_mandar;
}

