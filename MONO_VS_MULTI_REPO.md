# Mono-repo vs Multi-repo: Decision Guide

## Quick Comparison

| Aspect | Mono-repo | Multi-repo |
|---|---|---|
| **Setup Complexity** | ✅ Simple | ❌ Complex |
| **Code Sharing** | ✅ Easy (common module) | ❌ Via Maven artifacts |
| **CI/CD** | ✅ Single pipeline | ❌ Multiple pipelines |
| **Service Independence** | ❌ Coupled | ✅ Fully independent |
| **Team Ownership** | ❌ Shared ownership | ✅ Clear ownership |
| **Atomic Changes** | ✅ Single commit | ❌ Multiple PRs |
| **Build Time** | ❌ Builds all services | ✅ Builds only changed |
| **Assignment Demo** | ✅ Perfect | ❌ Overkill |
| **Production Scale** | ⚠️ Works for small teams | ✅ Better for large teams |

---

## When to Use Mono-repo

✅ **Use mono-repo when:**
- Small team (< 20 developers)
- Tight coupling between services
- Frequent cross-service changes
- Shared code is significant
- Assignment/demo/POC
- Startup/early-stage product

**Examples:**
- Google (uses mono-repo at massive scale)
- Facebook (mono-repo with custom tooling)
- Your assignment (perfect fit!)

---

## When to Use Multi-repo

✅ **Use multi-repo when:**
- Large team (> 20 developers)
- Services are truly independent
- Different teams own different services
- Independent release cycles needed
- Mature product with stable APIs
- Enterprise with strict access control

**Examples:**
- Netflix (hundreds of microservices)
- Amazon (service-oriented architecture)
- Most large enterprises

---

## Hybrid Approach (Best of Both)

Some companies use a hybrid:

```
mono-repo/
├── core-services/          # Tightly coupled services
│   ├── auth-service/
│   ├── user-service/
│   └── notification-service/
└── shared-libraries/

Separate repos:
├── payment-service/        # PCI compliance isolation
├── analytics-service/      # Different team
└── ml-recommendation/      # Different tech stack
```

---

## For Your Assignment: Use Mono-repo

**Reasons:**
1. ✅ Interviewer clones ONE repo
2. ✅ Run `docker-compose up` → everything works
3. ✅ Easier to review code
4. ✅ Shows you understand project structure
5. ✅ Can still explain multi-repo in discussion

**What to say in interview:**
> "I've implemented this as a mono-repo for easier demonstration. 
> In production, we could split into separate repositories per service 
> for independent deployment and team ownership. The current structure 
> already supports this - each service has its own Dockerfile, CI/CD 
> pipeline, and can be deployed independently."

This shows you understand both approaches! 🎯

---

## Migration Path

If you start with mono-repo and need to split later:

```bash
# Extract service to separate repo
git subtree split -P services/auth-service -b auth-service-branch
cd ../auth-service-new-repo
git pull ../movie-booking-platform auth-service-branch

# Publish shared libraries to Maven
mvn deploy:deploy-file \
  -DgroupId=com.xyz.shared \
  -DartifactId=domain-events \
  -Dversion=1.0.0 \
  -Dpackaging=jar \
  -Dfile=target/domain-events-1.0.0.jar \
  -DrepositoryId=nexus \
  -Durl=https://nexus.xyz.com/repository/maven-releases/
```

---

## Conclusion

For your assignment: **Mono-repo is the right choice**. 

It's simpler, easier to demonstrate, and perfectly valid for microservices. 
You can always discuss the trade-offs and when you'd use multi-repo in the interview.
