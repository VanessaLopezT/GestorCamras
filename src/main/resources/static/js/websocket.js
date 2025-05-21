class WebSocketClient {
    constructor() {
        this.stompClient = null;
        this.connected = false;
    }

    connect(onConnect, onDisconnect, onError) {
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        this.stompClient.connect({}, (frame) => {
            console.log('Conectado a WebSocket:', frame);
            this.connected = true;
            if (onConnect) onConnect();
            
            // Suscribirse a actualizaciones de equipos
            this.subscribeToEquipos();
            
        }, (error) => {
            console.error('Error en la conexión WebSocket:', error);
            this.connected = false;
            if (onError) onError(error);
            
            // Reintentar conexión después de 5 segundos
            setTimeout(() => this.connect(onConnect, onDisconnect, onError), 5000);
        });
        
        // Manejar desconexión
        socket.onclose = () => {
            console.log('Conexión WebSocket cerrada');
            this.connected = false;
            if (onDisconnect) onDisconnect();
            
            // Reintentar conexión después de 5 segundos
            setTimeout(() => this.connect(onConnect, onDisconnect, onError), 5000);
        };
    }
    
    subscribeToEquipos() {
        if (!this.connected || !this.stompClient) return;
        
        // Suscribirse a actualizaciones de equipos
        this.stompClient.subscribe('/topic/equipos/update', (message) => {
            const equipo = JSON.parse(message.body);
            console.log('Equipo actualizado:', equipo);
            this.handleEquipoUpdate(equipo);
        });
        
        // Suscribirse a nuevas conexiones de equipos
        this.stompClient.subscribe('/topic/equipos/connected', (message) => {
            const equipo = JSON.parse(message.body);
            console.log('Nuevo equipo conectado:', equipo);
            this.handleNuevoEquipo(equipo);
        });
    }
    
    handleEquipoUpdate(equipo) {
        // Actualizar la interfaz de usuario con los datos del equipo
        if (window.updateEquipoUI) {
            window.updateEquipoUI(equipo);
        }
    }
    
    handleNuevoEquipo(equipo) {
        // Agregar el nuevo equipo a la interfaz de usuario
        if (window.addNuevoEquipoUI) {
            window.addNuevoEquipoUI(equipo);
        } else {
            // Si la función no está disponible, recargar la lista de equipos
            if (window.cargarEquipos) {
                window.cargarEquipos();
            }
        }
    }
    
    disconnect() {
        if (this.stompClient !== null) {
            this.stompClient.disconnect();
        }
        this.connected = false;
    }
}

// Inicializar el cliente WebSocket cuando se cargue la página
document.addEventListener('DOMContentLoaded', () => {
    const wsClient = new WebSocketClient();
    
    // Hacer el cliente accesible globalmente
    window.wsClient = wsClient;
    
    // Conectar al servidor WebSocket
    wsClient.connect(
        () => console.log('WebSocket conectado correctamente'),
        () => console.log('WebSocket desconectado'),
        (error) => console.error('Error en WebSocket:', error)
    );
    
    // Manejar cierre de la página
    window.addEventListener('beforeunload', () => {
        wsClient.disconnect();
    });
});
