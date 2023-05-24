package com.example.drive;

import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@RestController
public class LoginController {

    public static final IDS iDS = null;
    public LoginController()
    {
    }
    class Session
    {
        public UUID Id = UUID.randomUUID();
        public LocalDateTime LastLogin = LocalDateTime.MIN;

        public Session(UUID id)
        {
            Id = id;
            LastLogin = LocalDateTime.now();
        }
        public String toString()
        {
            return Id+";"+LastLogin;
        }
    }
    class IDS
    {
        public ArrayList<Session> Sessions = new ArrayList<Session>();
        public IDS()
        {
        }
        public Session Get(UUID guid)
        {
            for (Session session : Sessions)
            {
                if (session.Id.equals(guid))
                    return session;
            }
            return null;
        }
        public boolean Contains(UUID guid)
        {
            for(Session session : Sessions)
            {
                if(session.Id.equals(guid))
                    return true;
            }
            return false;
        }
        public void Add(UUID session)
        {
            Sessions.add(new Session(session));
        }
        public Session Last()
        {
            return Sessions.get(Sessions.size()-1);
        }
    }
}
