package com.vehisales.platform.api;

import com.vehisales.platform.api.dto.admin.BrandRecord;
import com.vehisales.platform.api.dto.admin.CatalogSnapshot;
import com.vehisales.platform.api.dto.admin.CategoryRecord;
import com.vehisales.platform.api.dto.admin.DealerPolicyRecord;
import com.vehisales.platform.api.dto.admin.DealerRecord;
import com.vehisales.platform.api.dto.admin.FeeDefinitionRecord;
import com.vehisales.platform.api.dto.admin.FeePolicyRecord;
import com.vehisales.platform.api.dto.admin.FeeRuleRecord;
import com.vehisales.platform.api.dto.admin.ImportResult;
import com.vehisales.platform.api.dto.admin.LocationRecord;
import com.vehisales.platform.api.dto.admin.PlateRegionsRecord;
import com.vehisales.platform.api.dto.admin.TranslateRequest;
import com.vehisales.platform.api.dto.admin.TranslateResponse;
import com.vehisales.platform.api.dto.admin.VehicleRecord;
import com.vehisales.platform.service.CatalogAdminService;
import com.vehisales.platform.service.PolicyAdminService;
import com.vehisales.platform.service.TextTranslateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CatalogAdminService catalogAdminService;
    private final PolicyAdminService policyAdminService;
    private final TextTranslateService textTranslateService;

    @GetMapping("/fee-policy")
    public FeePolicyRecord getFeePolicy() {
        return policyAdminService.getFeePolicy();
    }

    @PutMapping("/fee-policy")
    public FeePolicyRecord saveFeePolicy(@RequestBody FeePolicyRecord record) {
        return policyAdminService.saveFeePolicy(record);
    }

    @GetMapping("/dealer-policy")
    public DealerPolicyRecord getDealerPolicy() {
        return policyAdminService.getDealerPolicy();
    }

    @PutMapping("/dealer-policy")
    public DealerPolicyRecord saveDealerPolicy(@RequestBody DealerPolicyRecord record) {
        return policyAdminService.saveDealerPolicy(record);
    }

    @GetMapping("/license-plate-regions")
    public PlateRegionsRecord getPlateRegions() {
        return policyAdminService.getPlateRegions();
    }

    @PutMapping("/license-plate-regions")
    public PlateRegionsRecord savePlateRegions(@RequestBody PlateRegionsRecord record) {
        return policyAdminService.savePlateRegions(record);
    }

    @PostMapping("/translate")
    public TranslateResponse translate(@RequestBody TranslateRequest request) {
        return textTranslateService.translate(request == null ? "" : request.text());
    }

    @GetMapping("/catalog")
    public CatalogSnapshot exportCatalog() {
        return catalogAdminService.exportAll();
    }

    @PostMapping("/import")
    public ImportResult importCatalog(@RequestBody CatalogSnapshot snapshot) {
        return catalogAdminService.importAll(snapshot);
    }

    @GetMapping("/brands")
    public List<BrandRecord> listBrands() {
        return catalogAdminService.listBrands();
    }

    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public BrandRecord createBrand(@RequestBody BrandRecord record) {
        return catalogAdminService.upsertBrand(new BrandRecord(
                null,
                record.code(),
                record.name(),
                record.tagline(),
                record.market(),
                record.accentColor(),
                record.imageUrl(),
                record.ready(),
                record.sortOrder()
        ));
    }

    @PutMapping("/brands/{id}")
    public BrandRecord updateBrand(@PathVariable Long id, @RequestBody BrandRecord record) {
        return catalogAdminService.upsertBrand(new BrandRecord(
                id,
                record.code(),
                record.name(),
                record.tagline(),
                record.market(),
                record.accentColor(),
                record.imageUrl(),
                record.ready(),
                record.sortOrder()
        ));
    }

    @DeleteMapping("/brands/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrand(@PathVariable Long id) {
        catalogAdminService.deleteBrand(id);
    }

    @GetMapping("/categories")
    public List<CategoryRecord> listCategories() {
        return catalogAdminService.listCategories();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryRecord createCategory(@RequestBody CategoryRecord record) {
        return catalogAdminService.upsertCategory(withId(record, null));
    }

    @PutMapping("/categories/{id}")
    public CategoryRecord updateCategory(@PathVariable Long id, @RequestBody CategoryRecord record) {
        return catalogAdminService.upsertCategory(withId(record, id));
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        catalogAdminService.deleteCategory(id);
    }

    @GetMapping("/locations")
    public List<LocationRecord> listLocations() {
        return catalogAdminService.listLocations();
    }

    @PostMapping("/locations")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationRecord createLocation(@RequestBody LocationRecord record) {
        return catalogAdminService.upsertLocation(withId(record, null));
    }

    @PutMapping("/locations/{id}")
    public LocationRecord updateLocation(@PathVariable Long id, @RequestBody LocationRecord record) {
        return catalogAdminService.upsertLocation(withId(record, id));
    }

    @DeleteMapping("/locations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long id) {
        catalogAdminService.deleteLocation(id);
    }

    @GetMapping("/dealers")
    public List<DealerRecord> listDealers() {
        return catalogAdminService.listDealers();
    }

    @PostMapping("/dealers")
    @ResponseStatus(HttpStatus.CREATED)
    public DealerRecord createDealer(@RequestBody DealerRecord record) {
        return catalogAdminService.upsertDealer(withId(record, null));
    }

    @PutMapping("/dealers/{id}")
    public DealerRecord updateDealer(@PathVariable Long id, @RequestBody DealerRecord record) {
        return catalogAdminService.upsertDealer(withId(record, id));
    }

    @DeleteMapping("/dealers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDealer(@PathVariable Long id) {
        catalogAdminService.deleteDealer(id);
    }

    @GetMapping("/fee-definitions")
    public List<FeeDefinitionRecord> listFeeDefinitions() {
        return catalogAdminService.listFeeDefinitions();
    }

    @PostMapping("/fee-definitions")
    @ResponseStatus(HttpStatus.CREATED)
    public FeeDefinitionRecord createFeeDefinition(@RequestBody FeeDefinitionRecord record) {
        return catalogAdminService.upsertFeeDefinition(withId(record, null));
    }

    @PutMapping("/fee-definitions/{id}")
    public FeeDefinitionRecord updateFeeDefinition(@PathVariable Long id, @RequestBody FeeDefinitionRecord record) {
        return catalogAdminService.upsertFeeDefinition(withId(record, id));
    }

    @DeleteMapping("/fee-definitions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeeDefinition(@PathVariable Long id) {
        catalogAdminService.deleteFeeDefinition(id);
    }

    @GetMapping("/vehicles")
    public List<VehicleRecord> listVehicles() {
        return catalogAdminService.listVehicles();
    }

    @PostMapping("/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleRecord createVehicle(@RequestBody VehicleRecord record) {
        return catalogAdminService.upsertVehicle(withId(record, null));
    }

    @PutMapping("/vehicles/{id}")
    public VehicleRecord updateVehicle(@PathVariable Long id, @RequestBody VehicleRecord record) {
        return catalogAdminService.upsertVehicle(withId(record, id));
    }

    @DeleteMapping("/vehicles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long id) {
        catalogAdminService.deleteVehicle(id);
    }

    @GetMapping("/fee-rules")
    public List<FeeRuleRecord> listFeeRules() {
        return catalogAdminService.listFeeRules();
    }

    @PostMapping("/fee-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public FeeRuleRecord createFeeRule(@RequestBody FeeRuleRecord record) {
        return catalogAdminService.upsertFeeRule(withId(record, null));
    }

    @PutMapping("/fee-rules/{id}")
    public FeeRuleRecord updateFeeRule(@PathVariable Long id, @RequestBody FeeRuleRecord record) {
        return catalogAdminService.upsertFeeRule(withId(record, id));
    }

    @DeleteMapping("/fee-rules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFeeRule(@PathVariable Long id) {
        catalogAdminService.deleteFeeRule(id);
    }

    private static CategoryRecord withId(CategoryRecord record, Long id) {
        return new CategoryRecord(
                id,
                record.code(),
                record.name(),
                record.description(),
                record.typicalSeats(),
                record.requiresInspection(),
                record.requiresRoadUseFee(),
                record.requiresCompulsoryInsurance(),
                record.sortOrder()
        );
    }

    private static LocationRecord withId(LocationRecord record, Long id) {
        return new LocationRecord(
                id,
                record.code(),
                record.name(),
                record.nameEn(),
                record.nameZh(),
                record.nameJa(),
                record.region(),
                record.feeZone(),
                record.centrallyGovernedCity()
        );
    }

    private static DealerRecord withId(DealerRecord record, Long id) {
        return new DealerRecord(id, record.brandCode(), record.name(), record.address(), record.market(), record.active());
    }

    private static FeeDefinitionRecord withId(FeeDefinitionRecord record, Long id) {
        return new FeeDefinitionRecord(
                id,
                record.code(),
                record.name(),
                record.description(),
                record.mandatory(),
                record.sortOrder(),
                record.active()
        );
    }

    private static VehicleRecord withId(VehicleRecord record, Long id) {
        return new VehicleRecord(
                id,
                record.brandCode(),
                record.categoryCode(),
                record.model(),
                record.name(),
                record.seats(),
                record.vehicleType(),
                record.year(),
                record.engineCc(),
                record.fuelType(),
                record.transmission(),
                record.listPrice(),
                record.discountAmount(),
                record.salePrice(),
                record.taxBasePrice(),
                record.defaultDeposit(),
                record.registrationServiceFee(),
                record.micaPlateFee(),
                record.inspectionFee(),
                record.defaultColor(),
                record.availableColors(),
                record.colorPhotos(),
                record.deliveryNote(),
                record.warrantyNote(),
                record.gifts(),
                record.quoteSheetName(),
                record.imageUrl(),
                record.specifications(),
                record.active()
        );
    }

    private static FeeRuleRecord withId(FeeRuleRecord record, Long id) {
        return new FeeRuleRecord(
                id,
                record.feeDefinitionCode(),
                record.categoryCode(),
                record.locationCode(),
                record.feeZone(),
                record.calculationType(),
                record.fixedAmount(),
                record.percentage(),
                record.minAmount(),
                record.maxAmount(),
                record.minEngineCc(),
                record.maxEngineCc(),
                record.minPrice(),
                record.maxPrice(),
                record.priority(),
                record.effectiveFrom(),
                record.effectiveTo(),
                record.active()
        );
    }
}
