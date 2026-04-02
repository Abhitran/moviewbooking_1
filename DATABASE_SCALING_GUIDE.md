# Database Scaling Guide - Beyond 1TB

## The 1TB Myth - Busted! 🚀

**Common Misconception**: "Beyond 1TB data can't be stored in relational databases"

**Reality**: Modern relational databases can handle **much more than 1TB**!

| Database | Maximum Storage Capacity | Real-World Examples |
|---|---|---|
| PostgreSQL | **Unlimited** (theoretically) | Instagram (multi-TB), Uber (10+ TB) |
| AWS RDS PostgreSQL | **64 TB** per instance | Discord (4+ TB per cluster) |
| Aurora PostgreSQL | **128 TB** per cluster | Notion (multi-TB) |
| MySQL | 64 TB | Shopify, GitHub |
| Oracle | **Unlimited** (with partitioning) | Banks, Fortune 500 |
| SQL Server | **524 PB** (petabytes!) | Microsoft, enterprises |

---

## RDS vs PostgreSQL - What's the Difference?

**Short Answer**: We're using **both** - RDS is AWS's managed service that runs PostgreSQL.

```
┌─────────────────────────────────────┐
│         AWS RDS Service             │
│  (Managed: backups, patching, HA)  │
│                                     │
│    ┌─────────────────────────┐     │
│    │   PostgreSQL Engine     │     │
│    │  (Actual database)      │     │
│    └─────────────────────────┘     │
└─────────────────────────────────────┘
```

**Think of it like**:
- PostgreSQL = The car engine
- RDS = The full car with maintenance, insurance, and support

**What RDS provides**:
- ✅ Automated backups (daily snapshots)
- ✅ Automated patching (security updates)
- ✅ Multi-AZ deployment (high availability)
- ✅ Read replicas (scaling reads)
- ✅ Monitoring (CloudWatch integration)
- ✅ Auto-scaling storage (up to 64 TB)

---

## Our Database Growth Projections

| Year | Bookings | Users | Shows | DB Size | Strategy | Monthly Cost |
|---|---|---|---|---|---|---|
| **Year 1** | 10M | 1M | 500K | **50 GB** | Single RDS | $650 |
| **Year 2** | 50M | 5M | 2M | **250 GB** | Single RDS | $650 |
| **Year 3** | 150M | 10M | 5M | **750 GB** | Single RDS | $650 |
| **Year 5** | 500M | 30M | 15M | **2.5 TB** | Partitioning + Replicas | $2,000 |
| **Year 10** | 2B | 100M | 50M | **10 TB** | Sharding + Archival | $4,000 |

**Key Insight**: We won't hit 1TB until Year 4-5, giving us plenty of time to optimize!

---

## Scaling Strategy Roadmap

### Phase 1: Vertical Scaling (Year 1-3, < 1 TB)

**Simplest approach**: Just upgrade the instance size

```
Starting:  db.r6g.xlarge    (4 vCPU, 32 GB RAM, 500 GB)   → $650/month
Scale up:  db.r6g.2xlarge   (8 vCPU, 64 GB RAM, 1 TB)    → $1,300/month
Scale up:  db.r6g.4xlarge   (16 vCPU, 128 GB RAM, 2 TB)  → $2,600/month
Scale up:  db.r6g.8xlarge   (32 vCPU, 256 GB RAM, 5 TB)  → $5,200/month
Maximum:   db.r6g.16xlarge  (64 vCPU, 512 GB RAM, 64 TB) → $10,400/month
```

**How to scale**:
```bash
# AWS CLI
aws rds modify-db-instance \
  --db-instance-identifier booking-db \
  --db-instance-class db.r6g.2xlarge \
  --apply-immediately

# Or click a button in AWS Console
```

**Pros**:
- ✅ Zero code changes
- ✅ Takes 5-10 minutes
- ✅ Automatic failover (Multi-AZ)

**Cons**:
- ❌ Gets expensive at large scale
- ❌ Downtime during upgrade (5-10 min)

---

### Phase 2: Table Partitioning (Year 3-5, 1-3 TB)

**Goal**: Split large tables into smaller, manageable chunks

#### Why Partition?

**Without Partitioning**:
```sql
SELECT * FROM bookings WHERE booking_date = '2024-01-15';
-- Scans entire 2 TB table → 30 seconds
```

**With Partitioning**:
```sql
SELECT * FROM bookings WHERE booking_date = '2024-01-15';
-- Scans only bookings_2024_01 partition (10 GB) → 0.3 seconds
-- 100x faster!
```

#### Implementation

**Step 1: Create Partitioned Table**
```sql
-- Parent table (no data stored here)
CREATE TABLE bookings (
    booking_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    show_id UUID NOT NULL,
    booking_date DATE NOT NULL,
    total_amount DECIMAL(10,2),
    status VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) PARTITION BY RANGE (booking_date);

-- Create monthly partitions
CREATE TABLE bookings_2024_01 PARTITION OF bookings
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE bookings_2024_02 PARTITION OF bookings
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');

CREATE TABLE bookings_2024_03 PARTITION OF bookings
    FOR VALUES FROM ('2024-03-01') TO ('2024-04-01');

-- ... and so on
```

**Step 2: Automate Partition Creation**
```sql
-- Function to create next month's partition
CREATE OR REPLACE FUNCTION create_next_partition()
RETURNS void AS $$
DECLARE
    next_month DATE := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '1 month');
    partition_name TEXT := 'bookings_' || TO_CHAR(next_month, 'YYYY_MM');
    start_date DATE := next_month;
    end_date DATE := next_month + INTERVAL '1 month';
BEGIN
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF bookings
         FOR VALUES FROM (%L) TO (%L)',
        partition_name, start_date, end_date
    );
END;
$$ LANGUAGE plpgsql;

-- Schedule via cron (run monthly)
SELECT cron.schedule('create-partition', '0 0 1 * *', 'SELECT create_next_partition()');
```

**Step 3: Drop Old Partitions**
```sql
-- Drop partitions older than 2 years
DROP TABLE IF EXISTS bookings_2022_01;
DROP TABLE IF EXISTS bookings_2022_02;
-- ... etc
```

#### Partition Strategy by Table

| Table | Partition Key | Partition Size | Retention |
|---|---|---|---|
| `bookings` | `booking_date` | Monthly | 2 years |
| `payments` | `created_at` | Monthly | 2 years |
| `shows` | `show_date` | Quarterly | 1 year |
| `users` | No partition | N/A | Forever |
| `theatres` | No partition | N/A | Forever |

#### Benefits

- ✅ **10-100x faster queries** (only scan relevant partitions)
- ✅ **Easy data deletion** (drop old partitions instead of DELETE)
- ✅ **Better vacuum performance** (vacuum per partition)
- ✅ **Transparent to application** (queries work the same)
- ✅ **Supports thousands of partitions**

---

### Phase 3: Read Replicas (Year 3-5, High Read Traffic)

**Goal**: Distribute read load across multiple database instances

#### Architecture

```
                    ┌─────────────────────────┐
                    │   Primary RDS Instance  │
                    │   (All Writes)          │
                    │   - Bookings            │
                    │   - Payments            │
                    │   - User registration   │
                    └────────────┬────────────┘
                                 │
                        Async Replication
                        (< 1 second lag)
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                        │
   ┌────▼─────┐           ┌─────▼────┐           ┌──────▼────┐
   │ Replica 1│           │ Replica 2│           │ Replica 3 │
   │ (Search) │           │(History) │           │(Analytics)│
   │          │           │          │           │           │
   │ Theatre  │           │ User     │           │ Reports   │
   │ search   │           │ bookings │           │ Dashboards│
   └──────────┘           └──────────┘           └───────────┘
```

#### Configuration

**Spring Boot Configuration**:
```yaml
spring:
  datasource:
    primary:
      jdbc-url: jdbc:postgresql://primary.rds.amazonaws.com:5432/booking_db
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5
    
    replica-search:
      jdbc-url: jdbc:postgresql://replica-1.rds.amazonaws.com:5432/booking_db
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
      hikari:
        maximum-pool-size: 50  # Higher for read-heavy queries
        minimum-idle: 10
    
    replica-history:
      jdbc-url: jdbc:postgresql://replica-2.rds.amazonaws.com:5432/booking_db
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
      hikari:
        maximum-pool-size: 30
```

**Java Configuration**:
```java
@Configuration
public class DataSourceConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.replica-search")
    public DataSource replicaSearchDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.replica-history")
    public DataSource replicaHistoryDataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

**Service Layer**:
```java
@Service
public class TheatreService {
    
    @Autowired
    @Qualifier("primaryDataSource")
    private JdbcTemplate primaryJdbc;
    
    @Autowired
    @Qualifier("replicaSearchDataSource")
    private JdbcTemplate replicaSearchJdbc;
    
    // Writes go to primary
    @Transactional
    public Theatre createTheatre(Theatre theatre) {
        return theatreRepository.save(theatre);  // Uses primary
    }
    
    // Reads go to replica
    @Transactional(readOnly = true)
    public List<Theatre> searchTheatres(String city, String movie) {
        // Uses replica-search (read-only)
        return replicaSearchJdbc.query(
            "SELECT * FROM theatres WHERE city = ? AND movie = ?",
            new TheatreRowMapper(), city, movie
        );
    }
}
```

#### Read Replica Usage

| Replica | Purpose | Queries | Load |
|---|---|---|---|
| **Primary** | All writes | Bookings, payments, user registration | 10% |
| **Replica 1** | Search | Theatre search, show search | 50% |
| **Replica 2** | History | User booking history, profile | 30% |
| **Replica 3** | Analytics | Reports, dashboards, admin | 10% |

#### Benefits

- ✅ **Distributes read load** (90% of queries are reads)
- ✅ **Primary handles only writes** (faster)
- ✅ **Up to 15 read replicas** in RDS
- ✅ **Automatic failover** if primary fails
- ✅ **Low replication lag** (< 1 second)

#### Replication Lag Handling

```java
@Service
public class BookingService {
    
    public Booking createBooking(BookingRequest request) {
        // Write to primary
        Booking booking = bookingRepository.save(booking);
        
        // Read from primary immediately after write
        // (avoid replication lag)
        return bookingRepository.findById(booking.getId())
            .orElseThrow();
    }
    
    public List<Booking> getUserBookings(UUID userId) {
        // Read from replica (can tolerate 1-second lag)
        return bookingRepository.findByUserId(userId);
    }
}
```

---

### Phase 4: Data Archival (Year 5+, 2-10 TB)

**Goal**: Move old data to cheaper storage (S3)

#### Storage Cost Comparison

| Storage Type | Cost per GB/month | 1 TB/month | 10 TB/month |
|---|---|---|---|
| **RDS PostgreSQL** | $0.115 | $115 | $1,150 |
| **S3 Standard** | $0.023 | $23 | $230 |
| **S3 Intelligent-Tiering** | $0.023-$0.004 | $15 | $150 |
| **S3 Glacier** | $0.004 | $4 | $40 |

**Savings Example**:
```
Year 5: 2.5 TB total data
- Active (last 2 years): 1 TB in RDS → $115/month
- Archived (older): 1.5 TB in S3 → $35/month
- Total: $150/month

Without archival: $288/month (all in RDS)
Savings: $138/month = $1,656/year
```

#### Implementation

**Step 1: Export to S3**
```sql
-- Export bookings older than 2 years
COPY (
    SELECT * FROM bookings 
    WHERE booking_date < CURRENT_DATE - INTERVAL '2 years'
    ORDER BY booking_date
) TO PROGRAM 'aws s3 cp - s3://booking-archive/bookings/year=2022/bookings_2022.parquet'
WITH (FORMAT PARQUET);
```

**Step 2: Delete from RDS**
```sql
-- Delete archived data
DELETE FROM bookings 
WHERE booking_date < CURRENT_DATE - INTERVAL '2 years';

-- Reclaim space
VACUUM FULL bookings;
```

**Step 3: Query Archived Data (AWS Athena)**
```sql
-- Create external table pointing to S3
CREATE EXTERNAL TABLE archived_bookings (
    booking_id STRING,
    user_id STRING,
    show_id STRING,
    booking_date DATE,
    total_amount DECIMAL(10,2)
)
STORED AS PARQUET
LOCATION 's3://booking-archive/bookings/';

-- Query archived data
SELECT COUNT(*), SUM(total_amount)
FROM archived_bookings
WHERE booking_date BETWEEN '2022-01-01' AND '2022-12-31';
```

#### Archival Schedule

| Data Type | Retention in RDS | Archive to S3 | Archive to Glacier |
|---|---|---|---|
| Bookings | 2 years | 2-5 years | > 5 years |
| Payments | 2 years | 2-7 years | > 7 years |
| Shows | 1 year | 1-3 years | > 3 years |
| Users | Forever | Never | Never |
| Audit logs | 1 year | 1-7 years | > 7 years |

---

### Phase 5: Database Sharding (Year 10+, 10+ TB)

**Goal**: Split database across multiple instances

**When to shard**: Only when single instance hits 10+ TB and performance degrades

#### Sharding Strategies

**Option 1: Shard by City**
```
Shard 1: Mumbai, Pune → RDS instance 1 (2 TB)
Shard 2: Delhi, Gurgaon → RDS instance 2 (2 TB)
Shard 3: Bangalore, Hyderabad → RDS instance 3 (2 TB)
Shard 4: Chennai, Kolkata → RDS instance 4 (2 TB)
```

**Option 2: Shard by User ID**
```
Shard 1: user_id hash % 4 == 0 → RDS instance 1
Shard 2: user_id hash % 4 == 1 → RDS instance 2
Shard 3: user_id hash % 4 == 2 → RDS instance 3
Shard 4: user_id hash % 4 == 3 → RDS instance 4
```

#### Implementation

```java
@Service
public class ShardingService {
    private final Map<Integer, DataSource> shards = new HashMap<>();
    
    public DataSource getShardForUser(UUID userId) {
        int shardId = Math.abs(userId.hashCode()) % totalShards;
        return shards.get(shardId);
    }
    
    public DataSource getShardForCity(String city) {
        return cityToShardMapping.get(city);
    }
}

@Service
public class BookingService {
    @Autowired
    private ShardingService shardingService;
    
    public Booking createBooking(BookingRequest request) {
        // Route to correct shard
        DataSource shard = shardingService.getShardForUser(request.getUserId());
        
        // Use shard-specific repository
        BookingRepository repo = new BookingRepository(shard);
        return repo.save(booking);
    }
}
```

#### Challenges

- ❌ **Cross-shard queries** are difficult
- ❌ **Rebalancing shards** is complex
- ❌ **Distributed transactions** across shards
- ❌ **Application complexity** increases significantly

**Recommendation**: Avoid sharding until absolutely necessary (10+ TB)

---

## Alternative: Aurora PostgreSQL

For databases > 10 TB, consider **Aurora PostgreSQL**:

| Feature | RDS PostgreSQL | Aurora PostgreSQL | Winner |
|---|---|---|---|
| Max storage | 64 TB | **128 TB** | Aurora |
| Read replicas | 15 | 15 | Tie |
| Failover time | 60-120 sec | **< 30 sec** | Aurora |
| Performance | 1x | **3x faster** | Aurora |
| Replication lag | 1-5 sec | **< 100ms** | Aurora |
| Cost (db.r6g.xlarge) | $650/month | $900/month | RDS |
| Auto-scaling storage | Manual | **Automatic** | Aurora |

**When to migrate to Aurora**:
- Database > 10 TB
- Need < 30 second failover
- High read throughput (100k+ QPS)
- Budget allows 38% premium

**Migration**:
```bash
# Zero-downtime migration using AWS DMS
aws dms create-replication-task \
  --source-endpoint rds-booking-instance \
  --target-endpoint aurora-booking-cluster \
  --migration-type full-load-and-cdc
```

---

## Real-World Examples

### Instagram (1 billion users)
- **Database**: PostgreSQL
- **Size**: Multi-TB per cluster
- **Strategy**: Sharding by user_id, read replicas, aggressive caching

### Uber (100M+ users)
- **Database**: PostgreSQL
- **Size**: 10+ TB per cluster
- **Strategy**: Sharding by city, partitioning by date, read replicas

### Discord (150M+ users)
- **Database**: PostgreSQL
- **Size**: 4+ TB per cluster
- **Strategy**: Sharding by server_id, partitioning, read replicas

### Notion (30M+ users)
- **Database**: PostgreSQL
- **Size**: Multi-TB
- **Strategy**: Sharding by workspace, partitioning, Aurora

---

## Summary

### Myth Busted
**"Beyond 1TB data can't be stored in relational DB"** → **FALSE!**

Modern PostgreSQL can handle:
- ✅ 64 TB in RDS
- ✅ 128 TB in Aurora
- ✅ Unlimited with sharding
- ✅ Proven at Instagram, Uber, Discord scale

### Our Scaling Path

1. **Year 1-3** (< 1 TB): Single RDS → Simple, cheap
2. **Year 3-5** (1-3 TB): Partitioning + Replicas → Optimized
3. **Year 5-10** (3-10 TB): Archival + More replicas → Cost-effective
4. **Year 10+** (10+ TB): Sharding or Aurora → Massive scale

### Key Takeaway

We won't hit 1TB until Year 4-5, and PostgreSQL can easily handle 10-64 TB with proper optimization. By the time we need sharding (100+ TB), we'll have the revenue to support it! 🎯

---

**What to say in interview**:
> "We're using AWS RDS PostgreSQL, which supports up to 64 TB per instance. Our projections show we'll reach 750 GB by Year 3, well within limits. For growth beyond 1 TB, we'll use table partitioning by date, read replicas for read-heavy queries, and archive old data to S3. This is the same strategy used by companies like Instagram and Uber with multi-TB PostgreSQL databases. The myth that relational databases can't handle more than 1TB is outdated - modern PostgreSQL can scale to 64 TB in RDS, 128 TB in Aurora, and unlimited with sharding."
