# Debug Application for debugging http-session confusion

# Brief
Get identity of user from session went wrong, and UA received cookie belongs to other owner? <br>
There may be a race condition between two different http worker threads? <br>

# Problem
I can't figure out why 'commitSession' method in SessionRepositoryFilter get different
session-id around 'doFilter()', the former is called 'requestedSessionId' acquired by
'getRequestedSessionId()' and the latter is called 'sessionId' acquired by 'session.getId()'

# Keypoint that produce this problem
1. Make RequestAttributes visible to other threads at line 38 in [DebugServiceImpl#whoami](/debug-app/src/main/java/com/acech/debug/service/impl/DebugServiceImpl.java)
2. Start async threads that poll results from some http-endpoints at line 56 in [DebugServiceImpl#whoami](/debug-app/src/main/java/com/acech/debug/service/impl/DebugServiceImpl.java)
3. Results in at line 244 in SessionRepositoryFilter set account's cookie to wrong client
4. Wrong assigned client may carry two Cookie attribute with same name 'Session', which confused
   SessionRepositoryFilter#httpSessionIdResolver

# How to reproduce the problem
1. Build with `mvn clean build` and start docker-compose services using `docker-compose up`
2. Add following pairs to your /etc/hosts
    ```text
    127.0.0.1 debug-redis
    127.0.0.1 debug-cas
    127.0.0.1 debug-app
    ```
3. Open your browser and type http://debug-app:8080/api/debug/whoami
4. Then you should be redirected to https://debug-cas:8443/cas/login
5. Login with any username with password 'Mellon', eg: user1
6. Then you should be redirected to http://debug-app:8080/api/debug/whoami
7. Press F12 to open your browser devtools, get the cookie
8. Start an infinite loop that send request to http://debug-app:8080/api/debug/whoami 
with the pre-acquired cookie using tools like Postman
9. Do step 5 ~ 8 again, now you get two infinite request loop
10. Do step 5 again, and refresh the webpage repeatedly after redirect
11. Assume that you have logged in with three account 'userA', 'userB' and 'userC'
, you may get wrong username each time you refresh the webpage
12. Assume that you have logged in with three account 'userA', 'userB' and 'userC'
, you may get wrong response 'There maybe a race condition which read another request's cookie' 
in your loop requests, represents that client has received other owner's cookie