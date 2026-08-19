package com.vehisales.platform.config;

import com.vehisales.platform.domain.Brand;
import com.vehisales.platform.domain.FeeDefinition;
import com.vehisales.platform.domain.FeeRule;
import com.vehisales.platform.domain.Location;
import com.vehisales.platform.domain.Vehicle;
import com.vehisales.platform.domain.VehicleCategory;
import com.vehisales.platform.domain.enums.FeeCalculationType;
import com.vehisales.platform.domain.enums.FeeZone;
import com.vehisales.platform.domain.enums.Region;
import com.vehisales.platform.repository.BrandRepository;
import com.vehisales.platform.repository.FeeDefinitionRepository;
import com.vehisales.platform.repository.FeeRuleRepository;
import com.vehisales.platform.repository.LocationRepository;
import com.vehisales.platform.repository.VehicleCategoryRepository;
import com.vehisales.platform.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final BrandRepository brandRepository;
    private final VehicleCategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final VehicleRepository vehicleRepository;
    private final FeeDefinitionRepository feeDefinitionRepository;
    private final FeeRuleRepository feeRuleRepository;

    @Override
    @Transactional
    public void run(String... args) {
        feeRuleRepository.deleteAll();
        vehicleRepository.deleteAll();
        feeDefinitionRepository.deleteAll();
        categoryRepository.deleteAll();
        locationRepository.deleteAll();
        brandRepository.deleteAll();

        Map<String, Brand> brands = seedBrands();
        Map<String, VehicleCategory> categories = seedCategories();
        seedLocations();
        Map<String, FeeDefinition> fees = seedFeeDefinitions();
        seedFeeRules(categories, fees);
        seedMitsubishiVietnam(brands.get("MITSUBISHI"), categories);
    }

    private Map<String, Brand> seedBrands() {
        List<Brand> created = brandRepository.saveAll(List.of(
                brand("MITSUBISHI", "Mitsubishi", "Official Vietnam lineup and on-road cost.", "Vietnam",
                        "#E60012", "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?auto=format&fit=crop&w=1400&q=80", true, 10),
                brand("VINFAST", "VinFast", "Vietnamese EV brand. Catalog coming soon.", "Vietnam",
                        "#146C43", "https://images.unsplash.com/photo-1593941707882-a5bba14938c7?auto=format&fit=crop&w=1400&q=80", false, 20),
                brand("HONDA", "Honda", "Cars and motorcycles. Catalog coming soon.", "Vietnam",
                        "#E40521", "https://images.unsplash.com/photo-1619767886558-efdc259cde1a?auto=format&fit=crop&w=1400&q=80", false, 30),
                brand("TOYOTA", "Toyota", "Passenger cars and SUVs. Catalog coming soon.", "Vietnam",
                        "#EB0A1E", "https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=1400&q=80", false, 40)
        ));
        Map<String, Brand> map = new LinkedHashMap<>();
        created.forEach(item -> map.put(item.getCode(), item));
        return map;
    }

    private Map<String, VehicleCategory> seedCategories() {
        List<VehicleCategory> created = categoryRepository.saveAll(List.of(
                category("MOTORCYCLE", "Motorcycle", "Motorbikes and scooters", 2, false, false, true, 10),
                category("BICYCLE", "Bicycle", "Pedal and e-bikes", 1, false, false, false, 20),
                category("PASSENGER_CAR_4", "Passenger car – 4 seats", "Sedans, hatchbacks and compact SUVs", 5, true, true, true, 30),
                category("PASSENGER_CAR_7", "Passenger car – 7 seats", "MPVs and family SUVs", 7, true, true, true, 40),
                category("PICKUP", "Pickup truck", "Light pickup trucks", 5, true, true, true, 50),
                category("TRUCK", "Truck", "Commercial trucks", 3, true, true, true, 60),
                category("VAN", "Van", "Passenger and commercial vans", 12, true, true, true, 70),
                category("OTHER", "Other", "Specialty vehicles", null, false, false, true, 80)
        ));
        Map<String, VehicleCategory> map = new LinkedHashMap<>();
        created.forEach(item -> map.put(item.getCode(), item));
        return map;
    }

    private void seedLocations() {
        locationRepository.saveAll(List.of(
                loc("HN", "Hà Nội", "Hanoi", "河内", "ハノイ", Region.NORTH, FeeZone.SPECIAL, true),
                loc("HP", "Hải Phòng", "Hai Phong", "海防", "ハイフォン", Region.NORTH, FeeZone.MAJOR, true),
                loc("BN", "Bắc Ninh", "Bac Ninh", "北宁", "バクニン", Region.NORTH, FeeZone.STANDARD, false),
                loc("CB", "Cao Bằng", "Cao Bang", "高平", "カオバン", Region.NORTH, FeeZone.STANDARD, false),
                loc("DB", "Điện Biên", "Dien Bien", "奠边", "ディエンビエン", Region.NORTH, FeeZone.STANDARD, false),
                loc("HY", "Hưng Yên", "Hung Yen", "兴安", "フンイエン", Region.NORTH, FeeZone.STANDARD, false),
                loc("LC", "Lai Châu", "Lai Chau", "莱州", "ライチャウ", Region.NORTH, FeeZone.STANDARD, false),
                loc("LS", "Lạng Sơn", "Lang Son", "谅山", "ランソン", Region.NORTH, FeeZone.STANDARD, false),
                loc("LO", "Lào Cai", "Lao Cai", "老街", "ラオカイ", Region.NORTH, FeeZone.STANDARD, false),
                loc("NB", "Ninh Bình", "Ninh Binh", "宁平", "ニンビン", Region.NORTH, FeeZone.STANDARD, false),
                loc("PT", "Phú Thọ", "Phu Tho", "富寿", "フート", Region.NORTH, FeeZone.STANDARD, false),
                loc("QN", "Quảng Ninh", "Quang Ninh", "广宁", "クアンニン", Region.NORTH, FeeZone.STANDARD, false),
                loc("SL", "Sơn La", "Son La", "山罗", "ソンラ", Region.NORTH, FeeZone.STANDARD, false),
                loc("TN", "Thái Nguyên", "Thai Nguyen", "太原", "タイグエン", Region.NORTH, FeeZone.STANDARD, false),
                loc("TQ", "Tuyên Quang", "Tuyen Quang", "宣光", "トゥエンクアン", Region.NORTH, FeeZone.STANDARD, false),
                loc("HUE", "Huế", "Hue", "顺化", "フエ", Region.CENTRAL, FeeZone.MAJOR, true),
                loc("DN", "Đà Nẵng", "Da Nang", "岘港", "ダナン", Region.CENTRAL, FeeZone.MAJOR, true),
                loc("HT", "Hà Tĩnh", "Ha Tinh", "河静", "ハティン", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("KH", "Khánh Hòa", "Khanh Hoa", "庆和", "カインホア", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("NA", "Nghệ An", "Nghe An", "义安", "ゲアン", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("QNG", "Quảng Ngãi", "Quang Ngai", "广义", "クアンガイ", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("QT", "Quảng Trị", "Quang Tri", "广治", "クアンチ", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("TH", "Thanh Hóa", "Thanh Hoa", "清化", "タインホア", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("GL", "Gia Lai", "Gia Lai", "嘉莱", "ザライ", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("DLK", "Đắk Lắk", "Dak Lak", "得乐", "ダクラク", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("LD", "Lâm Đồng", "Lam Dong", "林同", "ラムドン", Region.CENTRAL, FeeZone.STANDARD, false),
                loc("HCM", "Thành phố Hồ Chí Minh", "Ho Chi Minh City", "胡志明市", "ホーチミン市", Region.SOUTH, FeeZone.SPECIAL, true),
                loc("CT", "Cần Thơ", "Can Tho", "芹苴", "カントー", Region.SOUTH, FeeZone.MAJOR, true),
                loc("AG", "An Giang", "An Giang", "安江", "アンザン", Region.SOUTH, FeeZone.STANDARD, false),
                loc("CM", "Cà Mau", "Ca Mau", "金瓯", "カマウ", Region.SOUTH, FeeZone.STANDARD, false),
                loc("DNI", "Đồng Nai", "Dong Nai", "同奈", "ドンナイ", Region.SOUTH, FeeZone.STANDARD, false),
                loc("DT", "Đồng Tháp", "Dong Thap", "同塔", "ドンタップ", Region.SOUTH, FeeZone.STANDARD, false),
                loc("TNI", "Tây Ninh", "Tay Ninh", "西宁", "タイニン", Region.SOUTH, FeeZone.STANDARD, false),
                loc("VL", "Vĩnh Long", "Vinh Long", "永隆", "ヴィンロン", Region.SOUTH, FeeZone.STANDARD, false)
        ));
    }

    private Map<String, FeeDefinition> seedFeeDefinitions() {
        List<FeeDefinition> created = feeDefinitionRepository.saveAll(List.of(
                fee("LICENSE_PLATE", "License plate issuance fee", "Fee to issue a vehicle license plate.", true, 10),
                fee("REGISTRATION_FEE", "Vehicle registration fee", "Administrative fee to register the vehicle.", true, 20),
                fee("REGISTRATION_TAX", "Registration tax", "Registration tax, usually a percentage of list price.", true, 30),
                fee("ROAD_USE", "Road use / maintenance fee", "Annual road-use fee for cars, vans and trucks.", true, 40),
                fee("INSPECTION", "Vehicle inspection fee", "Periodic inspection for cars, vans and trucks.", true, 50),
                fee("COMPULSORY_INSURANCE", "Compulsory civil liability insurance", "Mandatory third-party liability insurance.", true, 60),
                fee("OPTIONAL_BODY_INSURANCE", "Optional body / physical damage insurance", "Optional physical-damage cover.", false, 70)
        ));
        Map<String, FeeDefinition> map = new LinkedHashMap<>();
        created.forEach(item -> map.put(item.getCode(), item));
        return map;
    }

    private void seedFeeRules(Map<String, VehicleCategory> categories, Map<String, FeeDefinition> fees) {
        LocalDate from = LocalDate.of(2026, 1, 1);
        for (String car : List.of("PASSENGER_CAR_4", "PASSENGER_CAR_7", "PICKUP", "TRUCK", "VAN")) {
            plate(fees, categories, car, FeeZone.SPECIAL, 20_000_000, from);
            plate(fees, categories, car, FeeZone.MAJOR, 1_000_000, from);
            plate(fees, categories, car, FeeZone.STANDARD, 200_000, from);
            fixed(fees, categories, "REGISTRATION_FEE", car, 150_000, from);
        }
        plate(fees, categories, "MOTORCYCLE", FeeZone.SPECIAL, 4_000_000, from);
        plate(fees, categories, "MOTORCYCLE", FeeZone.MAJOR, 1_000_000, from);
        plate(fees, categories, "MOTORCYCLE", FeeZone.STANDARD, 150_000, from);
        plate(fees, categories, "OTHER", FeeZone.SPECIAL, 4_000_000, from);
        plate(fees, categories, "OTHER", FeeZone.MAJOR, 500_000, from);
        plate(fees, categories, "OTHER", FeeZone.STANDARD, 150_000, from);
        fixed(fees, categories, "REGISTRATION_FEE", "MOTORCYCLE", 50_000, from);
        fixed(fees, categories, "REGISTRATION_FEE", "BICYCLE", 0, from);
        fixed(fees, categories, "REGISTRATION_FEE", "OTHER", 50_000, from);

        percentZone(fees, categories, "REGISTRATION_TAX", "PASSENGER_CAR_4", FeeZone.SPECIAL, "12", from);
        percentZone(fees, categories, "REGISTRATION_TAX", "PASSENGER_CAR_4", FeeZone.MAJOR, "10", from);
        percentZone(fees, categories, "REGISTRATION_TAX", "PASSENGER_CAR_4", FeeZone.STANDARD, "10", from);
        percentZone(fees, categories, "REGISTRATION_TAX", "PASSENGER_CAR_7", FeeZone.SPECIAL, "12", from);
        percentZone(fees, categories, "REGISTRATION_TAX", "PASSENGER_CAR_7", FeeZone.MAJOR, "10", from);
        percentZone(fees, categories, "REGISTRATION_TAX", "PASSENGER_CAR_7", FeeZone.STANDARD, "10", from);
        percent(fees, categories, "REGISTRATION_TAX", "MOTORCYCLE", "2", from);
        percent(fees, categories, "REGISTRATION_TAX", "PICKUP", "2", from);
        percent(fees, categories, "REGISTRATION_TAX", "TRUCK", "2", from);
        percent(fees, categories, "REGISTRATION_TAX", "VAN", "10", from);
        percent(fees, categories, "REGISTRATION_TAX", "OTHER", "2", from);

        fixed(fees, categories, "ROAD_USE", "PASSENGER_CAR_4", 1_560_000, from);
        fixed(fees, categories, "ROAD_USE", "PASSENGER_CAR_7", 1_800_000, from);
        fixed(fees, categories, "ROAD_USE", "PICKUP", 2_160_000, from);
        fixed(fees, categories, "ROAD_USE", "TRUCK", 3_600_000, from);
        fixed(fees, categories, "ROAD_USE", "VAN", 2_160_000, from);

        fixed(fees, categories, "INSPECTION", "PASSENGER_CAR_4", 340_000, from);
        fixed(fees, categories, "INSPECTION", "PASSENGER_CAR_7", 340_000, from);
        fixed(fees, categories, "INSPECTION", "PICKUP", 350_000, from);
        fixed(fees, categories, "INSPECTION", "TRUCK", 560_000, from);
        fixed(fees, categories, "INSPECTION", "VAN", 350_000, from);

        engineFixed(fees, categories, "COMPULSORY_INSURANCE", "MOTORCYCLE", 0, 50, 60_500, from);
        engineFixed(fees, categories, "COMPULSORY_INSURANCE", "MOTORCYCLE", 51, 9999, 66_000, from);
        fixed(fees, categories, "COMPULSORY_INSURANCE", "PASSENGER_CAR_4", 480_700, from);
        fixed(fees, categories, "COMPULSORY_INSURANCE", "PASSENGER_CAR_7", 794_000, from);
        fixed(fees, categories, "COMPULSORY_INSURANCE", "PICKUP", 1_061_800, from);
        fixed(fees, categories, "COMPULSORY_INSURANCE", "TRUCK", 2_273_000, from);
        fixed(fees, categories, "COMPULSORY_INSURANCE", "VAN", 1_273_300, from);
        fixed(fees, categories, "COMPULSORY_INSURANCE", "OTHER", 66_000, from);

        percent(fees, categories, "OPTIONAL_BODY_INSURANCE", "MOTORCYCLE", "1.20", from);
        percent(fees, categories, "OPTIONAL_BODY_INSURANCE", "PASSENGER_CAR_4", "1.50", from);
        percent(fees, categories, "OPTIONAL_BODY_INSURANCE", "PASSENGER_CAR_7", "1.50", from);
        percent(fees, categories, "OPTIONAL_BODY_INSURANCE", "PICKUP", "1.80", from);
        percent(fees, categories, "OPTIONAL_BODY_INSURANCE", "TRUCK", "2.00", from);
        percent(fees, categories, "OPTIONAL_BODY_INSURANCE", "VAN", "1.80", from);
        percent(fees, categories, "OPTIONAL_BODY_INSURANCE", "OTHER", "1.50", from);
    }

    private void seedMitsubishiVietnam(Brand mitsubishi, Map<String, VehicleCategory> categories) {
        vehicleRepository.saveAll(List.of(
                vehicle(mitsubishi, categories, "Attrage", "Mitsubishi Attrage 1.2 CVT Premium", "PASSENGER_CAR_4",
                        5, "ICE", 2026, 1193, "Gasoline", "CVT", 458_000_000,
                        "https://images.unsplash.com/photo-1617814076367-b759c7d7e738?auto=format&fit=crop&w=1200&q=80",
                        specs("Power", "78 hp", "Drive", "FWD", "Airbags", "2", "Market", "Vietnam")),
                vehicle(mitsubishi, categories, "Xpander", "Mitsubishi Xpander 1.5 AT Premium", "PASSENGER_CAR_7",
                        7, "ICE", 2026, 1499, "Gasoline", "Automatic", 598_000_000,
                        "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?auto=format&fit=crop&w=1200&q=80",
                        specs("Power", "104 hp", "Seats", "7", "Ground clearance", "200 mm", "Market", "Vietnam")),
                vehicle(mitsubishi, categories, "Xpander Cross", "Mitsubishi Xpander Cross 1.5 AT", "PASSENGER_CAR_7",
                        7, "ICE", 2026, 1499, "Gasoline", "Automatic", 698_000_000,
                        "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=1200&q=80",
                        specs("Power", "104 hp", "Ground clearance", "225 mm", "Look", "Crossover MPV", "Market", "Vietnam")),
                vehicle(mitsubishi, categories, "Xforce", "Mitsubishi Xforce Ultimate", "PASSENGER_CAR_4",
                        5, "ICE", 2026, 1499, "Gasoline", "Automatic", 705_000_000,
                        "https://images.unsplash.com/photo-1617469767053-d3b523a0b982?auto=format&fit=crop&w=1200&q=80",
                        specs("Power", "104 hp", "Class", "B-SUV", "Drive", "FWD", "Market", "Vietnam")),
                vehicle(mitsubishi, categories, "Outlander", "Mitsubishi Outlander 2.0 CVT Premium", "PASSENGER_CAR_7",
                        7, "ICE", 2026, 1998, "Gasoline", "CVT", 1_129_000_000,
                        "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&w=1200&q=80",
                        specs("Power", "150 hp", "Seats", "7", "Safety", "MI-PILOT", "Market", "Vietnam")),
                vehicle(mitsubishi, categories, "Triton", "Mitsubishi Triton Athlete 4x4 AT", "PICKUP",
                        5, "ICE", 2026, 2442, "Diesel", "Automatic", 965_000_000,
                        "https://images.unsplash.com/photo-1605893477799-b99e3b8b93fe?auto=format&fit=crop&w=1200&q=80",
                        specs("Power", "181 hp", "Drive", "4WD", "Payload", "1,000 kg", "Market", "Vietnam")),
                vehicle(mitsubishi, categories, "Pajero Sport", "Mitsubishi Pajero Sport 2.4 4x2 AT", "PASSENGER_CAR_7",
                        7, "ICE", 2026, 2442, "Diesel", "Automatic", 1_268_000_000,
                        "https://images.unsplash.com/photo-1511919884226-fd3cad34687c?auto=format&fit=crop&w=1200&q=80",
                        specs("Power", "181 hp", "Drive", "RWD", "Body", "Body-on-frame", "Market", "Vietnam"))
        ));
    }

    private Brand brand(String code, String name, String tagline, String market, String color, String image, boolean ready, int sort) {
        return Brand.builder()
                .code(code)
                .name(name)
                .tagline(tagline)
                .market(market)
                .accentColor(color)
                .imageUrl(image)
                .ready(ready)
                .sortOrder(sort)
                .build();
    }

    private VehicleCategory category(String code, String name, String description, Integer seats,
                                    boolean inspection, boolean roadUse, boolean insurance, int sort) {
        return VehicleCategory.builder()
                .code(code).name(name).description(description).typicalSeats(seats)
                .requiresInspection(inspection).requiresRoadUseFee(roadUse)
                .requiresCompulsoryInsurance(insurance).sortOrder(sort)
                .build();
    }

    private Location loc(String code, String vi, String en, String zh, String ja, Region region, FeeZone zone, boolean city) {
        return Location.builder()
                .code(code).name(vi).nameEn(en).nameZh(zh).nameJa(ja)
                .region(region).feeZone(zone).centrallyGovernedCity(city)
                .build();
    }

    private FeeDefinition fee(String code, String name, String description, boolean mandatory, int sort) {
        return FeeDefinition.builder()
                .code(code).name(name).description(description)
                .mandatory(mandatory).sortOrder(sort).active(true)
                .build();
    }

    private void plate(Map<String, FeeDefinition> fees, Map<String, VehicleCategory> categories,
                       String category, FeeZone zone, long amount, LocalDate from) {
        feeRuleRepository.save(baseRule(fees.get("LICENSE_PLATE"), categories.get(category), from)
                .feeZone(zone).calculationType(FeeCalculationType.FIXED)
                .fixedAmount(BigDecimal.valueOf(amount)).build());
    }

    private void fixed(Map<String, FeeDefinition> fees, Map<String, VehicleCategory> categories,
                       String feeCode, String category, long amount, LocalDate from) {
        feeRuleRepository.save(baseRule(fees.get(feeCode), categories.get(category), from)
                .calculationType(FeeCalculationType.FIXED)
                .fixedAmount(BigDecimal.valueOf(amount)).build());
    }

    private void engineFixed(Map<String, FeeDefinition> fees, Map<String, VehicleCategory> categories,
                             String feeCode, String category, int minCc, int maxCc, long amount, LocalDate from) {
        feeRuleRepository.save(baseRule(fees.get(feeCode), categories.get(category), from)
                .calculationType(FeeCalculationType.FIXED)
                .fixedAmount(BigDecimal.valueOf(amount))
                .minEngineCc(minCc).maxEngineCc(maxCc).priority(20).build());
    }

    private void percent(Map<String, FeeDefinition> fees, Map<String, VehicleCategory> categories,
                         String feeCode, String category, String percentage, LocalDate from) {
        feeRuleRepository.save(baseRule(fees.get(feeCode), categories.get(category), from)
                .calculationType(FeeCalculationType.PERCENT_OF_LIST_PRICE)
                .percentage(new BigDecimal(percentage)).build());
    }

    private void percentZone(Map<String, FeeDefinition> fees, Map<String, VehicleCategory> categories,
                             String feeCode, String category, FeeZone zone, String percentage, LocalDate from) {
        feeRuleRepository.save(baseRule(fees.get(feeCode), categories.get(category), from)
                .feeZone(zone).calculationType(FeeCalculationType.PERCENT_OF_LIST_PRICE)
                .percentage(new BigDecimal(percentage)).priority(10).build());
    }

    private FeeRule.FeeRuleBuilder baseRule(FeeDefinition definition, VehicleCategory category, LocalDate from) {
        return FeeRule.builder().feeDefinition(definition).category(category)
                .priority(0).effectiveFrom(from).active(true);
    }

    private Vehicle vehicle(Brand brand, Map<String, VehicleCategory> categories, String model, String name,
                            String categoryCode, Integer seats, String type, int year, int engineCc,
                            String fuel, String transmission, long price, String image, Map<String, String> specs) {
        return Vehicle.builder()
                .brand(brand).model(model).name(name).category(categories.get(categoryCode))
                .seats(seats).vehicleType(type).year(year).engineCc(engineCc)
                .fuelType(fuel).transmission(transmission)
                .listPrice(BigDecimal.valueOf(price)).imageUrl(image)
                .specifications(specs).active(true)
                .build();
    }

    private Map<String, String> specs(String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
