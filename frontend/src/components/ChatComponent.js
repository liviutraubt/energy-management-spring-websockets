import React, { useState, useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { getCurrentUser } from '../apiService';
import './ChatComponent.css';

const ChatComponent = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [activeTab, setActiveTab] = useState('bot');
    const [messageInput, setMessageInput] = useState('');
    const [messages, setMessages] = useState({ bot: [], admin: [] });
    const [unreadCount, setUnreadCount] = useState(0);
    const [activeConversations, setActiveConversations] = useState([]);

    const clientRef = useRef(null);
    const user = getCurrentUser();
    const isAdmin = user?.role === 'ADMIN';
    const messagesEndRef = useRef(null);

    useEffect(() => {
        if (!user) return;

        const client = new Client({
            webSocketFactory: () => new SockJS('http://localhost:81/ws'),

            reconnectDelay: 5000,
            debug: (str) => {
            },
            onConnect: (frame) => {
                client.subscribe(`/topic/user/${user.id}`, (message) => {
                    if (message.body) {
                        handleIncomingMessage(JSON.parse(message.body));
                    }
                });

                if (isAdmin) {
                    client.subscribe('/topic/admin', (message) => {
                        if (message.body) {
                            handleIncomingMessage(JSON.parse(message.body));
                        }
                    });
                }
            },

            onStompError: (frame) => {
                console.error('Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            }
        });

        client.activate();
        clientRef.current = client;

        return () => {
            client.deactivate();
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, []);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, activeTab, isOpen]);

    const handleIncomingMessage = (message) => {
        if (!isOpen) {
            setUnreadCount(prev => prev + 1);
        }

        setMessages(prev => {
            const newMessages = { ...prev };
            let conversationKey = 'admin';

            if (message.senderId === 'BOT') {
                conversationKey = 'bot';
            } else if (isAdmin) {
                if (message.senderId === user.id) {
                    conversationKey = message.recipientId;
                } else {
                    conversationKey = message.senderId;
                }

                if (!activeConversations.includes(conversationKey) && conversationKey !== user.id) {
                    setActiveConversations(prevConvs => [...prevConvs, conversationKey]);
                }
            } else {
                conversationKey = 'admin';
            }

            if (!newMessages[conversationKey]) {
                newMessages[conversationKey] = [];
            }
            newMessages[conversationKey].push(message);
            return newMessages;
        });
    };

    const sendMessage = () => {
        if (!messageInput.trim() || !clientRef.current || !clientRef.current.connected) return;

        const chatMessage = {
            senderId: user.id,
            content: messageInput,
            isAdmin: isAdmin,
            recipientId: null
        };

        if (activeTab === 'bot') {
            clientRef.current.publish({
                destination: "/app/chat",
                body: JSON.stringify(chatMessage)
            });
            addMessageToState('bot', chatMessage);
        } else {
            if (isAdmin) {
                chatMessage.recipientId = activeTab;
                clientRef.current.publish({
                    destination: "/app/admin-chat",
                    body: JSON.stringify(chatMessage)
                });
                addMessageToState(activeTab, chatMessage);
            } else {
                clientRef.current.publish({
                    destination: "/app/admin-chat",
                    body: JSON.stringify(chatMessage)
                });
                addMessageToState('admin', chatMessage);
            }
        }

        setMessageInput('');
    };

    const addMessageToState = (key, msg) => {
        setMessages(prev => {
            const newState = { ...prev };
            if (!newState[key]) newState[key] = [];
            newState[key].push(msg);
            return newState;
        });
    };

    const toggleChat = () => {
        setIsOpen(!isOpen);
        if (!isOpen) setUnreadCount(0);
    };

    if (!user) return null;

    return (
        <div className="chat-container">
            <button className="chat-fab" onClick={toggleChat}>
                💬
                {unreadCount > 0 && <span className="notification-badge">{unreadCount}</span>}
            </button>

            {isOpen && (
                <div className="chat-window">
                    <div className="chat-header">
                        <h3>Asistență {isAdmin ? '(Admin)' : ''}</h3>
                        <button className="close-btn" onClick={toggleChat}>×</button>
                    </div>

                    <div className="chat-tabs">
                        {!isAdmin && (
                            <>
                                <button
                                    className={activeTab === 'bot' ? 'active' : ''}
                                    onClick={() => setActiveTab('bot')}>
                                    🤖 Bot
                                </button>
                                <button
                                    className={activeTab === 'admin' ? 'active' : ''}
                                    onClick={() => setActiveTab('admin')}>
                                    👨‍💼 Admin
                                </button>
                            </>
                        )}

                        {isAdmin && (
                            <div className="admin-tabs-list">
                                {activeConversations.length === 0 && <span style={{fontSize: '12px', padding: '5px'}}>Niciun mesaj nou</span>}
                                {activeConversations.map(userId => (
                                    <button
                                        key={userId}
                                        className={activeTab === userId ? 'active' : ''}
                                        onClick={() => setActiveTab(userId)}
                                    >
                                        User: {userId.substring(0, 6)}...
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>

                    <div className="chat-body">
                        {isAdmin && activeConversations.length === 0 && (
                            <div className="info-message">Așteptați mesaje...</div>
                        )}

                        {(messages[activeTab] || []).map((msg, idx) => (
                            <div
                                key={idx}
                                className={`message-bubble ${msg.senderId === user.id ? 'my-message' : 'other-message'}`}
                            >
                                <div className="msg-content">{msg.content}</div>
                                <div className="msg-sender">{msg.senderId === user.id ? 'Eu' : (msg.senderId === 'BOT' ? 'Bot' : 'User')}</div>
                            </div>
                        ))}
                        <div ref={messagesEndRef} />
                    </div>

                    <div className="chat-footer">
                        <input
                            type="text"
                            placeholder="Scrie un mesaj..."
                            value={messageInput}
                            onChange={(e) => setMessageInput(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                            disabled={isAdmin && !activeTab}
                        />
                        <button onClick={sendMessage} disabled={isAdmin && !activeTab}>➤</button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ChatComponent;